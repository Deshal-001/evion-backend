const sequelize = require("../config/db");
const env = require("../config/env");

const User = require("./user");
const UserProfile = require("./userProfile");
const Vehicle = require("./vehicle");
const Trip = require("./trip");
const ChargingStation = require("./chargingStation");

User.hasOne(UserProfile, { foreignKey: "userId", sourceKey: "id" });
UserProfile.belongsTo(User, { foreignKey: "userId", targetKey: "id" });

User.hasMany(Vehicle, { foreignKey: "userId", sourceKey: "id" });
Vehicle.belongsTo(User, { foreignKey: "userId", targetKey: "id" });

Vehicle.hasMany(Trip, { foreignKey: "vehicleId", sourceKey: "id" });
Trip.belongsTo(Vehicle, { foreignKey: "vehicleId", targetKey: "id" });

User.hasMany(Trip, { foreignKey: "userId", sourceKey: "id" });
Trip.belongsTo(User, { foreignKey: "userId", targetKey: "id" });

async function connectDatabase() {
  await sequelize.authenticate();
  if (env.sequelizeSync) {
    await sequelize.sync();
  }
}

module.exports = {
  sequelize,
  connectDatabase,
  User,
  UserProfile,
  Vehicle,
  Trip,
  ChargingStation
};
