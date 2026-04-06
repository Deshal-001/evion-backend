const { Vehicle } = require("../models");
const HttpError = require("../utils/httpError");

async function createVehicle(payload) {
  return Vehicle.create(payload);
}

async function getVehicle(id) {
  const vehicle = await Vehicle.findByPk(id);
  if (!vehicle) {
    throw new HttpError(404, "Vehicle not found");
  }
  return vehicle;
}

async function getVehiclesByUser(userId) {
  return Vehicle.findAll({ where: { userId } });
}

async function updateVehicle(id, payload) {
  const vehicle = await getVehicle(id);
  vehicle.name = payload.name;
  vehicle.model = payload.model;
  vehicle.licensePlate = payload.licensePlate;
  vehicle.batteryCapacityKwh = payload.batteryCapacityKwh;
  vehicle.evEmissionsKgPerKm = payload.evEmissionsKgPerKm;
  vehicle.consumptionPerKm = payload.consumptionPerKm;
  await vehicle.save();
  return vehicle;
}

async function deleteVehicle(id) {
  const vehicle = await getVehicle(id);
  await vehicle.destroy();
}

module.exports = {
  createVehicle,
  getVehicle,
  getVehiclesByUser,
  updateVehicle,
  deleteVehicle
};
