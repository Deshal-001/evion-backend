package com.evion.evion_backend.routeplanner.service;

import com.evion.evion_backend.batteryEnergySimulation.service.BatterySimulationService;
import com.evion.evion_backend.chargingStationManagement.model.ChargingStation;
import com.evion.evion_backend.chargingStationManagement.repository.ChargingStationRepository;
import com.evion.evion_backend.routeplanner.dto.Coordinate;
import com.evion.evion_backend.routeplanner.dto.RouteRequest;
import com.evion.evion_backend.routeplanner.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

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
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final BatterySimulationService batteryService;

    public RouteResponse planRoute(RouteRequest req) {
        JsonNode ors = callOrs(req);

        // parse polyline coords
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

        // Centralized consumption
        double consumption = batteryService.selectConsumption(
                req.getDrivingMode(),
                req.getConsumptionPerKm(),
                ecoConsumption,
                normalConsumption,
                aggressiveConsumption
        );

        //Centralized usable range
        double usableRangeKm = batteryService.computeUsableRangeKm(
                req.getBatteryCapacityKwh(),
                req.getCurrentChargePct(),
                consumption,
                reserveFactor
        );

        // estimated energy
        double estimatedEnergy = distanceKm * consumption;

        // if trip possible without charging
        List<Long> suggestedStops = new ArrayList<>();
        String message;
        if (usableRangeKm >= distanceKm) {
            message = "Trip possible without charging (reserve applied)";
        } else {
            List<ChargingStation> allStations = stationRepository.findAll();

            List<ChargingStation> reachableStations = allStations.parallelStream()
                    .filter(st -> haversine(req.getStartLat(), req.getStartLng(),
                    st.getLatitude(), st.getLongitude()) <= usableRangeKm)
                    .collect(Collectors.toList());

            if (!reachableStations.isEmpty()) {
                ChargingStation closest = reachableStations.stream()
                        .min(Comparator.comparingDouble(st
                                -> haversine(req.getStartLat(), req.getStartLng(),
                                st.getLatitude(), st.getLongitude())))
                        .orElseThrow();
                suggestedStops.add(closest.getId());
            }

            List<Long> routeStops = planChargingStopsAlongRoute(polyline, usableRangeKm, consumption, allStations,
                    batteryService.batteryOrFallback(req.getBatteryCapacityKwh()));
            suggestedStops.addAll(routeStops);

            message = suggestedStops.isEmpty()
                    ? "No charging stops found within search radius to cover the route"
                    : "Charging stops suggested along the route";
        }

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

    // --- Charging Stops ---
    private List<Long> planChargingStopsAlongRoute(List<Coordinate> polyline, double usableRangeKm,
            double consumptionPerKm, List<ChargingStation> allStations,
            double batteryKwh) {
        List<Long> stops = new ArrayList<>();
        if (usableRangeKm <= 0) {
            return stops;
        }

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

        while (idx < segDistKm.size() && remainingRouteKm > remainingRange) {
            double seg = segDistKm.get(idx);
            if (seg <= remainingRange) {
                remainingRange -= seg;
                remainingRouteKm -= seg;
                idx++;
                continue;
            } else {
                Coordinate from = polyline.get(idx);
                Coordinate to = polyline.get(idx + 1);
                double fraction = remainingRange / seg;
                Coordinate neededPoint = new Coordinate(
                        from.getLat() + (to.getLat() - from.getLat()) * fraction,
                        from.getLng() + (to.getLng() - from.getLng()) * fraction
                );

                Optional<ChargingStation> opt = findNearestStationWithin(allStations, neededPoint, stationSearchRadiusKm);
                if (opt.isPresent()) {
                    stops.add(opt.get().getId());
                    remainingRange = batteryService.computeUsableRangeKm(batteryKwh, 100.0, consumptionPerKm, reserveFactor);
                    idx++;
                } else {
                    return Collections.emptyList();
                }
            }
        }
        return stops;
    }

    // --- Utilities ---
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        /* same as before */
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        return haversine(lat1, lon1, lat2, lon2);
    }

    private Optional<ChargingStation> findNearestStationWithin(List<ChargingStation> stations, Coordinate point, double radiusKm) {
        return stations.parallelStream()
                .filter(s -> distanceKm(point.getLat(), point.getLng(), s.getLatitude(), s.getLongitude()) <= radiusKm)
                .min(Comparator.comparingDouble(s -> distanceKm(point.getLat(), point.getLng(), s.getLatitude(), s.getLongitude())));
    }

    private double totalDistanceKm(List<Double> segs) {
        return segs.stream().mapToDouble(Double::doubleValue).sum();
    }

    private int calculateEcoScore(double distanceKm, double energyKwh, double durationSec, String drivingMode) {
        /* same as before */
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

        return Math.max(0, Math.min(100, score));
    }

    private double round(double v, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(v * scale) / scale;
    }

    // --- ORS API Call ---
    private JsonNode callOrs(RouteRequest req) {
        try {
            String url = orsDirectionsUrl + "?api_key=" + orsApiKey;
            Map<String, Object> body = new HashMap<>();
            List<List<Double>> coordinates = Arrays.asList(
                    Arrays.asList(req.getStartLng(), req.getStartLat()),
                    Arrays.asList(req.getEndLng(), req.getEndLat())
            );
            body.put("coordinates", coordinates);

            String response = restTemplate.postForObject(url, body, String.class);
            return mapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call ORS API", e);
        }
    }
}
