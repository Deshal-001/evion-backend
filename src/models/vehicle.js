const { DataTypes } = require("sequelize");
const sequelize = require("../config/db");

const Vehicle = sequelize.define(
  "Vehicle",
  {
    id: {
      type: DataTypes.BIGINT,
      autoIncrement: true,
      primaryKey: true
    },
    userId: {
      type: DataTypes.BIGINT,
      allowNull: true,
      field: "user_id"
    },
    name: {
      type: DataTypes.STRING,
      allowNull: true
    },
    model: {
      type: DataTypes.STRING,
      allowNull: true
    },
    licensePlate: {
      type: DataTypes.STRING,
      allowNull: true,
      field: "license_plate"
    },
    consumptionPerKm: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0.2,
      field: "consumption_per_km"
    },
    batteryCapacityKwh: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 60,
      field: "battery_capacity_kwh"
    },
    evEmissionsKgPerKm: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0.04,
      field: "ev_emissions_kg_per_km"
    }
  },
  {
    tableName: "vehicles",
    timestamps: false
  }
);

module.exports = Vehicle;
