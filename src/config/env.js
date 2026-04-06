const dotenv = require("dotenv");

dotenv.config();

module.exports = {
  port: Number(process.env.PORT || 8080),
  db: {
    host: process.env.DB_HOST || "localhost",
    port: Number(process.env.DB_PORT || 5433),
    name: process.env.DB_NAME || "Evion",
    user: process.env.DB_USER || "postgres",
    password: process.env.DB_PASSWORD || "2765"
  },
  jwt: {
    secret: process.env.JWT_SECRET || "EVIONjwtSecretKey_!@#2025StrongKey0987654321",
    expirationMs: Number(process.env.JWT_EXPIRATION_MS || 86400000)
  },
  corsOrigin: process.env.CORS_ORIGIN || "http://localhost:3000",
  ors: {
    apiKey: process.env.ORS_API_KEY || "",
    directionsUrl:
      process.env.ORS_DIRECTIONS_URL ||
      "https://api.openrouteservice.org/v2/directions/driving-car/geojson"
  },
  ev: {
    consumptionEco: Number(process.env.EV_CONSUMPTION_ECO || 0.15),
    consumptionNormal: Number(process.env.EV_CONSUMPTION_NORMAL || 0.2),
    consumptionAggressive: Number(process.env.EV_CONSUMPTION_AGGRESSIVE || 0.25),
    defaultBatteryKwh: Number(process.env.EV_DEFAULT_BATTERY_KWH || 60),
    stationSearchRadiusKm: Number(process.env.STATIONS_SEARCH_RADIUS_KM || 10),
    reserveFactor: Number(process.env.EV_RESERVE_FACTOR || 0.1)
  },
  sequelizeSync: (process.env.SEQUELIZE_SYNC || "true").toLowerCase() === "true"
};
