const { DataTypes } = require("sequelize");
const sequelize = require("../config/db");

const UserProfile = sequelize.define(
  "UserProfile",
  {
    userId: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      allowNull: false,
      field: "user_id"
    },
    name: {
      type: DataTypes.STRING,
      allowNull: true
    },
    email: {
      type: DataTypes.STRING,
      allowNull: true
    },
    totalTrips: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
      field: "total_trips"
    },
    totalDistanceKm: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "total_distance_km"
    },
    totalEcoScore: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "total_eco_score"
    },
    totalEnergyUsedKwh: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "total_energy_used_kwh"
    },
    totalCo2EmittedKg: {
      type: DataTypes.DOUBLE,
      allowNull: false,
      defaultValue: 0,
      field: "total_co2_emitted_kg"
    }
  },
  {
    tableName: "user_profiles",
    timestamps: false
  }
);

module.exports = UserProfile;
