const env = require("../config/env");

function selectConsumption(
  drivingMode,
  overrideConsumption,
  ecoConsumption,
  normalConsumption,
  aggressiveConsumption
) {
  if (overrideConsumption != null && overrideConsumption > 0) {
    return overrideConsumption;
  }

  if (!drivingMode) {
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

function computeUsableRangeKm(
  batteryKwh,
  currentPct,
  consumptionPerKm,
  reserveFactor
) {
  if (
    batteryKwh == null ||
    currentPct == null ||
    consumptionPerKm == null ||
    consumptionPerKm <= 0
  ) {
    return Number.MAX_VALUE;
  }

  const energyAvailableKwh = (currentPct / 100) * batteryKwh;
  const usableRange = energyAvailableKwh / consumptionPerKm;
  return usableRange * (1 - reserveFactor);
}

function batteryOrFallback(batteryKwh) {
  return batteryKwh != null && batteryKwh > 0
    ? batteryKwh
    : env.ev.defaultBatteryKwh;
}

function adjustConsumption(baseConsumption, drivingMode) {
  if (!drivingMode) {
    return baseConsumption;
  }

  switch (drivingMode.toUpperCase()) {
    case "ECO":
      return baseConsumption * 0.85;
    case "AGGRESSIVE":
      return baseConsumption * 1.15;
    default:
      return baseConsumption;
  }
}

function round(value) {
  return Math.round(value * 100) / 100;
}

function simulateBattery(request) {
  const capacity = request.batteryCapacityKwh;
  const currentCharge = capacity * (request.currentChargePct / 100);
  const adjustedConsumption = adjustConsumption(
    request.consumptionPerKm,
    request.drivingMode
  );

  const remainingRange = currentCharge / adjustedConsumption;
  const estimatedEnergy = request.tripDistanceKm * adjustedConsumption;

  const canCompleteTrip =
    remainingRange >= request.tripDistanceKm * (1 + env.ev.reserveFactor);

  let alertMessage = null;
  if (!canCompleteTrip) {
    alertMessage = "Insufficient range. Please plan a charging stop.";
  } else if (request.currentChargePct < 20) {
    alertMessage = "Warning: Battery below 20%. Consider charging soon.";
  }

  return {
    remainingRangeKm: round(remainingRange),
    estimatedEnergyUse: round(estimatedEnergy),
    canCompleteTrip,
    alertMessage
  };
}

module.exports = {
  selectConsumption,
  computeUsableRangeKm,
  batteryOrFallback,
  simulateBattery
};
