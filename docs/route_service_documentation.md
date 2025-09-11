# RouteService Documentation

## Overview

The `RouteService` class is responsible for **planning an electric vehicle (EV) route**, including:

- Calling external routing services (OpenRouteService, ORS) for the real route.
- Calculating **battery-aware charging stops** along the route.
- Estimating **energy consumption** and **eco score** based on driving style, route, and battery state.

This ensures the EV can safely complete a journey considering **battery capacity**, **current charge**, and **driving behavior**.

---

## Configuration Properties

| Property | Description |
|----------|-------------|
| `ors.api.key` | API key for ORS service. |
| `ors.directions.url` | URL for ORS routing API. |
| `ev.consumption.eco` | Energy consumption per km in ECO driving mode (kWh/km). |
| `ev.consumption.normal` | Energy consumption per km in normal driving mode. |
| `ev.consumption.aggressive` | Energy consumption per km in aggressive driving mode. |
| `stations.search.radius.km` | Search radius around a point to find nearby charging stations. |
| `ev.reserve.factor` | Fraction of battery reserved to avoid full depletion. |

---

## Key Methods and Calculations

### 1. Calling ORS API

The service calls ORS to obtain the route polyline and summary:

```java
JsonNode ors = callOrs(req);
```

- **Input:** Start and end coordinates (`RouteRequest`).
- **Output:** GeoJSON with:
  - `geometry.coordinates`: polyline `[lng, lat]`.
  - `summary.distance`: total distance in meters.
  - `summary.duration`: total duration in seconds.

---

### 2. Parsing Polyline and Route Summary

Coordinates are parsed from ORS response:

```java
List<Coordinate> polyline = ...; // Convert ORS [lng,lat] to Coordinate(lat,lng)
double distanceKm = summary.path("distance").asDouble() / 1000.0;
double durationSec = summary.path("duration").asDouble();
```

- **Polyline:** List of lat/lng points along the route.
- **Distance:** Converted from meters to kilometers.
- **Duration:** Route duration in seconds.

---

### 3. Selecting Energy Consumption

Consumption per km is chosen based on:

1. Explicit override by the user.
2. Driving mode: `ECO`, `NORMAL`, `AGGRESSIVE`.
3. Defaults if none specified.

```java
double consumption = selectConsumption(req.getDrivingMode(), req.getConsumptionPerKm());
```

---

### 4. Estimating Energy Needed for Trip

Total estimated energy in kWh:

```java
double estimatedEnergy = distanceKm * consumption;
```

- `distanceKm`: total route distance.
- `consumption`: kWh/km based on driving mode.

---

### 5. Computing Usable Range

Usable range based on current battery charge:

```java
double usableRangeKm = computeUsableRangeKm(
    req.getBatteryCapacityKwh(),
    req.getCurrentChargePct(),
    consumption
);
```

**Calculation:**

```
energyAvailableKwh = (currentChargePct / 100) * batteryCapacityKwh
usableRangeKm = (energyAvailableKwh / consumptionPerKm) * (1 - reserveFactor)
```

- Accounts for reserve to avoid running out of charge.

---

### 6. Planning Charging Stops

1. Walk along the route polyline, accumulating distance.
2. When accumulated distance exceeds `usableRangeKm`, find the nearest charging station within `stationSearchRadiusKm`.
3. Insert the charging station as a stop and reset remaining range.
4. Repeat until the destination is reachable.

```java
List<Long> stops = planChargingStopsAlongRoute(polyline, usableRangeKm, consumption);
```

- Uses Haversine formula to calculate distances.
- Selects stations based on proximity to the route.

---

### 7. Eco Score Calculation

Simplified point-based eco score:

```java
int ecoScore = calculateEcoScore(distanceKm, estimatedEnergy, durationSec, req.getDrivingMode());
```

- Adjusts score for:
  - Average speed (>120 km/h reduces score)
  - Driving style (`AGGRESSIVE` reduces, `ECO` increases)
  - Energy efficiency (consumption per km)
- Range: 0 to 100

---

## Real-World Example

**Scenario:**

- Route: Gothenburg, Sweden → Malmö, Sweden
- Distance: 280 km
- EV Battery: 60 kWh, current charge 50%
- Driving mode: ECO
- Consumption per km: 0.18 kWh/km
- Reserve factor: 10%

**Calculations:**

1. Usable energy: 60 kWh * 50% = 30 kWh
2. Usable range: 30 kWh / 0.18 kWh/km ≈ 166.7 km  
   After reserve: 166.7 * (1 - 0.1) ≈ 150 km
3. Since 150 km < 280 km, charging stops are needed
4. Algorithm searches stations along the route and suggests 2–3 stops to safely reach Malmö
5. Eco score is calculated based on distance, estimated energy, and ECO mode

**Outcome:**

- Suggested Stops: Charging station IDs `[101, 205]`
- Estimated Energy: 50.4 kWh
- Trip Duration: 3.5 hours
- Eco Score: 85/100

---

## Summary

`RouteService` combines **real route planning** with **battery-aware EV logic**, ensuring:

- Safe travel without running out of charge
- Optimized energy consumption
- User-friendly suggestions for charging stops
- Eco-aware scoring for driving efficiency

This documentation helps developers understand how **distance, energy, and charging stations** are calculated along with the eco score system.
```

