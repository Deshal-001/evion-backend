const { ChargingStation } = require("../models");
const HttpError = require("../utils/httpError");

async function getAllStations() {
  return ChargingStation.findAll();
}

async function getStation(id) {
  const station = await ChargingStation.findByPk(id);
  if (!station) {
    throw new HttpError(404, "Station not found");
  }
  return station;
}

async function addStation(payload) {
  return ChargingStation.create(payload);
}

async function updateStation(id, payload) {
  const station = await getStation(id);
  station.name = payload.name;
  station.latitude = payload.latitude;
  station.longitude = payload.longitude;
  station.chargingType = payload.chargingType;
  station.chargingSpeed = payload.chargingSpeed;
  station.costPerKwh = payload.costPerKwh;
  station.status = payload.status;
  await station.save();
  return station;
}

async function deleteStation(id) {
  const station = await getStation(id);
  await station.destroy();
}

module.exports = {
  getAllStations,
  getStation,
  addStation,
  updateStation,
  deleteStation
};
