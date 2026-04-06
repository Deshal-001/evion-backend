const { DataTypes } = require("sequelize");
const sequelize = require("../config/db");

const Trip = sequelize.define(
  "Trip",
  {
    id: {
      type: DataTypes.BIGINT,
      autoIncrement: true,
      primaryKey: true
    },
    userId: {
      type: DataTypes.BIGINT,
      allowNull: false,
      field: "user_id"
    },
    distanceKm: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      field: "distance_km"
    },
    avgSpeedKmh: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      field: "avg_speed_kmh"
    },
    energyUsedKwh: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      field: "energy_used_kwh"
    },
    ecoScore: {
      type: DataTypes.INTEGER,
      allowNull: false,
      field: "eco_score"
    },
    startBatteryPct: {
      type: DataTypes.DOUBLE,
      allowNull: true,
      field: "start_battery_pct"
    },
    endBatteryPct: {
      type: DataTypes.DOUBLE,
      allowNull: true,
      field: "end_battery_pct"
    },
    suggestedStationIds: {
      type: DataTypes.ARRAY(DataTypes.BIGINT),
      allowNull: true,
      field: "suggested_station_ids"
    },
    co2EmittedKg: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      field: "co2_emitted_kg"
    },
    vehicleId: {
      type: DataTypes.BIGINT,
      allowNull: false,
      field: "vehicle_id"
    },
    tripDate: {
      type: DataTypes.DATE,
      allowNull: false,
      field: "trip_date",
      defaultValue: DataTypes.NOW
    }
  },
  {
    tableName: "trips",
    timestamps: false
  }
);

module.exports = Trip;
