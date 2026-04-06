const { Trip, Vehicle } = require("../models");
const userProfileService = require("./userProfileService");
const {
  calculateEnergyUsed,
  calculateEvCo2
} = require("../utils/evionCalculations");
const { calculateEcoScore } = require("../utils/ecoScoreCalculator");
const HttpError = require("../utils/httpError");

async function logTrip(request) {
  const vehicle = await Vehicle.findByPk(request.vehicleId);
  if (!vehicle) {
    throw new HttpError(404, "Vehicle not found");
  }

  const avgSpeed =
    request.durationSec > 0
      ? request.distanceKm / (request.durationSec / 3600)
      : 0;

  const energyUsedKwh = calculateEnergyUsed(
    request.distanceKm,
    vehicle.consumptionPerKm
  );

  const co2EmittedKg = calculateEvCo2(
    request.distanceKm,
    vehicle.evEmissionsKgPerKm
  );

  const ecoScore = calculateEcoScore(
    request.distanceKm,
    energyUsedKwh,
    request.durationSec,
    request.drivingMode,
    request.startBatteryPct,
    request.endBatteryPct
  );

  const trip = await Trip.create({
    userId: request.userId,
    distanceKm: request.distanceKm,
    avgSpeedKmh: avgSpeed,
    energyUsedKwh,
    ecoScore,
    startBatteryPct: request.startBatteryPct,
    endBatteryPct: request.endBatteryPct,
    suggestedStationIds: request.suggestedStationIds || [],
    co2EmittedKg,
    vehicleId: request.vehicleId,
    tripDate: new Date()
  });

  await userProfileService.recordTrip(
    request.userId,
    request.distanceKm,
    ecoScore,
    energyUsedKwh,
    co2EmittedKg
  );

  return trip;
}

async function getTripHistory(userId) {
  return Trip.findAll({
    where: { userId },
    order: [["tripDate", "DESC"]]
  });
}

module.exports = {
  logTrip,
  getTripHistory
};
