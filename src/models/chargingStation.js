const { DataTypes } = require("sequelize");
const sequelize = require("../config/db");

const ChargingStation = sequelize.define(
  "ChargingStation",
  {
    id: {
      type: DataTypes.BIGINT,
      autoIncrement: true,
      primaryKey: true
    },
    name: {
      type: DataTypes.STRING,
      allowNull: true
    },
    latitude: {
      type: DataTypes.DOUBLE,
      allowNull: false
    },
    longitude: {
      type: DataTypes.DOUBLE,
      allowNull: false
    },
    chargingType: {
      type: DataTypes.STRING,
      allowNull: true,
      field: "charging_type"
    },
    chargingSpeed: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "charging_speed"
    },
    costPerKwh: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "cost_per_kwh"
    },
    status: {
      type: DataTypes.ENUM("AVAILABLE", "OCCUPIED", "OUT_OF_SERVICE"),
      allowNull: false,
      defaultValue: "AVAILABLE"
    }
  },
  {
    tableName: "charging_stations",
    timestamps: false
  }
);

module.exports = ChargingStation;
