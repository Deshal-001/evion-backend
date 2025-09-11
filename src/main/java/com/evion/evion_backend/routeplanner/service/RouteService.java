package com.evion.evion_backend.routeplanner.service;

import com.evion.evion_backend.chargingStationManagement.model.ChargingStation;
import com.evion.evion_backend.chargingStationManagement.repository.ChargingStationRepository;
import com.evion.evion_backend.routeplanner.dto.Coordinate;
import com.evion.evion_backend.routeplanner.dto.RouteRequest;
import com.evion.evion_backend.routeplanner.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RouteService: calls ORS for the real route and computes battery-aware
 * charging stops.
 */
@Service
@RequiredArgsConstructor
public class RouteService {

    @Value("${ors.api.key:}")
    private String orsApiKey;

    @Value("${ors.directions.url}")
    private String orsDirectionsUrl;

    @Value("${ev.consumption.eco}")
    private double ecoConsumption;

    @Value("${ev.consumption.normal}")
    private double normalConsumption;

    @Value("${ev.consumption.aggressive}")
    private double aggressiveConsumption;

    @Value("${stations.search.radius.km}")
    private double stationSearchRadiusKm;

    @Value("${ev.reserve.factor}")
    private double reserveFactor;

    private final ChargingStationRepository stationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public RouteResponse planRoute(RouteRequest req) {
        JsonNode ors = callOrs(req);

        // parse polyline coords (ORS returns [lng,lat])
        JsonNode coordsNode = ors.path("features").get(0).path("geometry").path("coordinates");
        List<Coordinate> polyline = new ArrayList<>();
        for (JsonNode pair : coordsNode) {
            double lng = pair.get(0).asDouble();
            double lat = pair.get(1).asDouble();
            polyline.add(new Coordinate(lat, lng));
        }
        if (polyline.isEmpty()) {
            throw new RuntimeException("Route returned no coordinates");
        }

        // parse summary
        JsonNode summary = ors.path("features").get(0).path("properties").path("summary");
        double distanceM = summary.path("distance").asDouble();
        double durationSec = summary.path("duration").asDouble();
        double distanceKm = distanceM / 1000.0;

        // consumption selection
        double consumption = selectConsumption(req.getDrivingMode(), req.getConsumptionPerKm());

        // estimate energy for whole trip
        double estimatedEnergy = distanceKm * consumption;

        // compute usable range from current battery (kWh -> km)
        double usableRangeKm = computeUsableRangeKm(req.getBatteryCapacityKwh(), req.getCurrentChargePct(), consumption);

        // if entire trip can be covered
        List<Long> suggestedStops = new ArrayList<>();
        String message = null;
        if (usableRangeKm >= distanceKm * (1.0 + reserveFactor)) {
            message = "Trip possible without charging (reserve applied)";
        } else {
            // Check stations reachable from the starting point
            List<ChargingStation> allStations = stationRepository.findAll();
            List<ChargingStation> reachableStations = allStations.stream()
                    .filter(st -> haversine(req.getStartLat(), req.getStartLng(),
                    st.getLatitude(), st.getLongitude()) <= usableRangeKm)
                    .toList();

            if (!reachableStations.isEmpty()) {
                // Suggest closest reachable station first (including start)
                ChargingStation closest = reachableStations.stream()
                        .min(Comparator.comparingDouble(st
                                -> haversine(req.getStartLat(), req.getStartLng(),
                                st.getLatitude(), st.getLongitude())))
                        .orElseThrow();
                suggestedStops.add(closest.getId());
            }

            // Continue with iterative planning along the route
            List<Long> routeStops = planChargingStopsAlongRoute(polyline, usableRangeKm, consumption);
            suggestedStops.addAll(routeStops);

            if (suggestedStops.isEmpty()) {
                message = "No charging stops found within search radius to cover the route";
            } else {
                message = "Charging stops suggested along the route";
            }
        }

        // calculate eco score using simplified point-based system
        int ecoScore = calculateEcoScore(distanceKm, estimatedEnergy, durationSec, req.getDrivingMode());

        return RouteResponse.builder()
                .distanceKm(round(distanceKm, 3))
                .durationSec(Math.round(durationSec))
                .estimatedEnergyKwh(round(estimatedEnergy, 3))
                .ecoScore(ecoScore)
                .polyline(polyline)
                .suggestedStationIds(suggestedStops)
                .message(message)
                .build();
    }

// Haversine helper for distance in km
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // Calls ORS directions endpoint
    private JsonNode callOrs(RouteRequest req) {
        Map<String, Object> body = new HashMap<>();
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(List.of(req.getStartLng(), req.getStartLat()));
        coordinates.add(List.of(req.getEndLng(), req.getEndLat()));
        body.put("coordinates", coordinates);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (orsApiKey != null && !orsApiKey.isBlank()) {
            headers.set("Authorization", orsApiKey);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(orsDirectionsUrl, HttpMethod.POST, entity, String.class);
        try {
            return mapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ORS response", e);
        }
    }

    // Select consumption: priority: explicit consumptionPerKm, driving mode, defaults
    private double selectConsumption(String drivingMode, Double overrideConsumption) {
        if (overrideConsumption != null && overrideConsumption > 0) {
            return overrideConsumption;
        }
        if (drivingMode == null) {
            return normalConsumption;
        }
        switch (drivingMode.toUpperCase()) {
            case "ECO":
                return ecoConsumption;
            case "AGGRESSIVE":
                return aggressiveConsumption;
            default:
                return normalConsumption;
        }
    }

    // Compute usable range in km from battery capacity KWh and currentChargePct
    private double computeUsableRangeKm(Double batteryKwh, Double currentPct, double consumptionPerKm) {
        if (batteryKwh == null || currentPct == null || consumptionPerKm <= 0) {
            // fallback: assume large range to not force stops when insufficient info
            return Double.MAX_VALUE;
        }
        double energyAvailableKwh = (currentPct / 100.0) * batteryKwh;
        double usableRange = energyAvailableKwh / consumptionPerKm;
        // subtract small margin to avoid edge-of-range
        return usableRange * (1.0 - reserveFactor);
    }

    /**
     * Plan stops by walking along polyline distance and inserting stations
     * where needed. Algorithm: - Walk along polyline accumulating distance. -
     * When accumulated distance since last charge >= usableRangeKm, search for
     * nearest station within stationSearchRadiusKm around that polyline point.
     * If found, add it as a stop, reset accumulated distance from that station
     * point and continue. - Stop when destination reachable.
     */
    private List<Long> planChargingStopsAlongRoute(List<Coordinate> polyline, double usableRangeKm, double consumptionPerKm) {
        List<Long> stops = new ArrayList<>();
        if (usableRangeKm <= 0) {
            return stops;
        }

        final List<ChargingStation> allStations = stationRepository.findAll();

        // Precompute segment distances between consecutive points
        int n = polyline.size();
        List<Double> segDistKm = new ArrayList<>(Collections.nCopies(Math.max(0, n - 1), 0.0));
        for (int i = 0; i < n - 1; i++) {
            Coordinate a = polyline.get(i);
            Coordinate b = polyline.get(i + 1);
            segDistKm.set(i, distanceKm(a.getLat(), a.getLng(), b.getLat(), b.getLng()));
        }

        double remainingRange = usableRangeKm;
        double remainingRouteKm = totalDistanceKm(segDistKm);
        int idx = 0;

        // walk the polyline
        while (idx < segDistKm.size() && remainingRouteKm > remainingRange) {
            // move along segments consuming range
            double seg = segDistKm.get(idx);
            if (seg <= remainingRange) {
                remainingRange -= seg;
                remainingRouteKm -= seg;
                idx++;
                continue;
            } else {
                // we cannot finish this segment with remainingRange: compute the point along this segment
                Coordinate from = polyline.get(idx);
                Coordinate to = polyline.get(idx + 1);
                double fraction = remainingRange / seg; // 0..1
                double pointLat = from.getLat() + (to.getLat() - from.getLat()) * fraction;
                double pointLng = from.getLng() + (to.getLng() - from.getLng()) * fraction;
                Coordinate neededPoint = new Coordinate(pointLat, pointLng);

                // search for nearest station within radius
                Optional<ChargingStation> opt = findNearestStationWithin(allStations, neededPoint, stationSearchRadiusKm);
                if (opt.isPresent()) {
                    ChargingStation st = opt.get();
                    stops.add(st.getId());

                    // reset remaining range as if we charged to full at that station (simple model)
                    remainingRange = computeUsableRangeKm(/* batteryKwh */reqBatteryOrFallback(), /* pct */ 100.0, consumptionPerKm);
                    // now compute distance from station point to current 'to' point in same segment
                    double distFromStationToSegmentEnd = distanceKm(st.getLatitude(), st.getLongitude(), to.getLat(), to.getLng());
                    // subtract the part already traveled in this segment and continue
                    remainingRouteKm -= remainingRange; // approximate: we charged fully and proceed
                    // To keep loop stable, advance idx a bit
                    idx++; // move forward (approximate)
                } else {
                    // no station nearby the would-be depletion point -> try extending radius or fail
                    // we will try scanning forward along polyline for next possible station within some lookahead segments
                    boolean foundForward = false;
                    int lookIdx = idx + 1;
                    double accumDist = seg - remainingRange; // remaining distance in current segment after we deplete
                    while (!foundForward && lookIdx < segDistKm.size()) {
                        double distToThisPoint = accumDist;
                        Coordinate candidatePoint = polyline.get(lookIdx);
                        // try find station near this later polyline point
                        Optional<ChargingStation> opt2 = findNearestStationWithin(allStations, candidatePoint, stationSearchRadiusKm);
                        if (opt2.isPresent()) {
                            stops.add(opt2.get().getId());
                            remainingRange = computeUsableRangeKm(reqBatteryOrFallback(), 100.0, consumptionPerKm);
                            // continue from lookIdx
                            remainingRouteKm -= distToThisPoint;
                            idx = lookIdx;
                            foundForward = true;
                        } else {
                            accumDist += segDistKm.get(lookIdx);
                            lookIdx++;
                        }
                    }
                    if (!foundForward) {
                        // no station found along forward lookahead
                        // give up: return what we have (empty if none) and message to user
                        return Collections.emptyList();
                    }
                }
            }
        }

        // if we reach here, either destination is now reachable or stops contain planned points
        return stops;
    }

    // finds nearest station within radiusKm of the given point
    private Optional<ChargingStation> findNearestStationWithin(List<ChargingStation> stations, Coordinate point, double radiusKm) {
        return stations.stream()
                .filter(s -> distanceKm(point.getLat(), point.getLng(), s.getLatitude(), s.getLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(s -> distanceKm(point.getLat(), point.getLng(), s.getLatitude(), s.getLongitude())))
                .findFirst();
    }

    // small helper: total distance
    private double totalDistanceKm(List<Double> segs) {
        return segs.stream().mapToDouble(Double::doubleValue).sum();
    }

    // Haversine distance (km)
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // a conservative fallback to battery capacity if not provided (you can replace with a default)
    private double reqBatteryOrFallback() {
        // For production, pass battery capacity via RouteRequest; here we assume a standard battery
        return 60.0;
    }

    // simplified eco score function (same as earlier)
    private int calculateEcoScore(double distanceKm, double energyKwh, double durationSec, String drivingMode) {
        int score = 100;
        double avgSpeed = (durationSec > 0) ? (distanceKm / (durationSec / 3600.0)) : 0.0;
        double consumptionPerKm = (distanceKm > 0) ? (energyKwh / distanceKm) : 0;

        if (avgSpeed > 120) {
            score -= 10;
        }
        if ("AGGRESSIVE".equalsIgnoreCase(drivingMode)) {
            score -= 15;
        }
        if (consumptionPerKm > 0.25) {
            score -= 10;
        }
        if ("ECO".equalsIgnoreCase(drivingMode)) {
            score += 10;
        }
        if (consumptionPerKm < 0.18) {
            score += 10;
        }

        score = Math.max(0, Math.min(100, score));
        return score;
    }

    private double round(double v, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(v * scale) / scale;
    }
}
