const env = require("../config/env");
const { ChargingStation } = require("../models");
const { calculateEcoScore } = require("../utils/ecoScoreCalculator");
const { callOrs } = require("../utils/orsApiHelper");
const { haversine, round } = require("../utils/geo");
const batteryService = require("./batterySimulationService");

async function planRoute(request) {
  const ors = await callOrs(request, env.ors.directionsUrl, env.ors.apiKey);

  const feature = ors?.features?.[0];
  if (!feature) {
    throw new Error("Route returned no features");
  }

  const coordsNode = feature.geometry?.coordinates || [];
  const polyline = coordsNode.map((pair) => ({
    lat: pair[1],
    lng: pair[0]
  }));

  if (!polyline.length) {
    throw new Error("Route returned no coordinates");
  }

  const summary = feature.properties?.summary || {};
  const distanceM = Number(summary.distance || 0);
  const durationSec = Number(summary.duration || 0);
  const distanceKm = distanceM / 1000;

  const consumption = batteryService.selectConsumption(
    request.drivingMode,
    request.consumptionPerKm,
    env.ev.consumptionEco,
    env.ev.consumptionNormal,
    env.ev.consumptionAggressive
  );

  const usableRangeKm = batteryService.computeUsableRangeKm(
    request.batteryCapacityKwh,
    request.currentChargePct,
    consumption,
    env.ev.reserveFactor
  );

  const estimatedEnergy = distanceKm * consumption;

  let message;
  const suggestedStops = [];

  if (usableRangeKm >= distanceKm) {
    message = "Trip possible without charging (reserve applied)";
  } else {
    const allStations = await ChargingStation.findAll();

    const reachableStations = allStations.filter(
      (station) =>
        haversine(
          request.startLat,
          request.startLng,
          station.latitude,
          station.longitude
        ) <= usableRangeKm
    );

    if (reachableStations.length > 0) {
      const closest = [...reachableStations].sort(
        (a, b) =>
          haversine(
            request.startLat,
            request.startLng,
            a.latitude,
            a.longitude
          ) -
          haversine(
            request.startLat,
            request.startLng,
            b.latitude,
            b.longitude
          )
      )[0];

      suggestedStops.push(closest.id);
    }

    const routeStops = planChargingStopsAlongRoute(
      polyline,
      usableRangeKm,
      consumption,
      allStations,
      batteryService.batteryOrFallback(request.batteryCapacityKwh)
    );

    suggestedStops.push(...routeStops);

    message = suggestedStops.length
      ? "Charging stops suggested along the route"
      : "No charging stops found within search radius to cover the route";
  }

  const ecoScore = calculateEcoScore(
    distanceKm,
    estimatedEnergy,
    durationSec,
    request.drivingMode,
    null,
    null
  );

  return {
    distanceKm: round(distanceKm, 3),
    durationSec: Math.round(durationSec),
    estimatedEnergyKwh: round(estimatedEnergy, 3),
    ecoScore,
    polyline,
    suggestedStationIds: suggestedStops,
    message
  };
}

function planChargingStopsAlongRoute(
  polyline,
  usableRangeKm,
  consumptionPerKm,
  allStations,
  batteryKwh
) {
  const stops = [];
  if (usableRangeKm <= 0) {
    return stops;
  }

  const segDistKm = [];
  for (let i = 0; i < polyline.length - 1; i += 1) {
    const a = polyline[i];
    const b = polyline[i + 1];
    segDistKm.push(haversine(a.lat, a.lng, b.lat, b.lng));
  }

  let remainingRange = usableRangeKm;
  let remainingRouteKm = segDistKm.reduce((acc, v) => acc + v, 0);
  let idx = 0;

  while (idx < segDistKm.length && remainingRouteKm > remainingRange) {
    const seg = segDistKm[idx];
    if (seg <= remainingRange) {
      remainingRange -= seg;
      remainingRouteKm -= seg;
      idx += 1;
      continue;
    }

    const from = polyline[idx];
    const to = polyline[idx + 1];
    const fraction = remainingRange / seg;

    const neededPoint = {
      lat: from.lat + (to.lat - from.lat) * fraction,
      lng: from.lng + (to.lng - from.lng) * fraction
    };

    const nearest = findNearestStationWithin(
      allStations,
      neededPoint,
      env.ev.stationSearchRadiusKm
    );

    if (!nearest) {
      return [];
    }

    stops.push(nearest.id);
    remainingRange = batteryService.computeUsableRangeKm(
      batteryKwh,
      100,
      consumptionPerKm,
      env.ev.reserveFactor
    );
    idx += 1;
  }

  return stops;
}

function findNearestStationWithin(stations, point, radiusKm) {
  let nearest = null;
  let minDistance = Number.MAX_VALUE;

  for (const station of stations) {
    const d = haversine(point.lat, point.lng, station.latitude, station.longitude);
    if (d <= radiusKm && d < minDistance) {
      minDistance = d;
      nearest = station;
    }
  }

  return nearest;
}

module.exports = {
  planRoute
};
