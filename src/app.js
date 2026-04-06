const express = require("express");
const cors = require("cors");
const swaggerUi = require("swagger-ui-express");
const YAML = require("yamljs");

const env = require("./config/env");
const errorHandler = require("./middleware/errorHandler");

const authRoutes = require("./routes/authRoutes");
const vehicleRoutes = require("./routes/vehicleRoutes");
const tripRoutes = require("./routes/tripRoutes");
const chargingStationRoutes = require("./routes/chargingStationRoutes");
const userProfileRoutes = require("./routes/userProfileRoutes");
const routeRoutes = require("./routes/routeRoutes");
const batteryRoutes = require("./routes/batteryRoutes");

const swaggerDocument = YAML.load("./docs/openapi.yaml");

const app = express();

app.use(
  cors({
    origin: env.corsOrigin,
    methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allowedHeaders: ["Content-Type", "Authorization"],
    credentials: true
  })
);
app.use(express.json());

app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use("/api/auth", authRoutes);
app.use("/api/vehicles", vehicleRoutes);
app.use("/api/trips", tripRoutes);
app.use("/api/stations", chargingStationRoutes);
app.use("/api/user", userProfileRoutes);
app.use("/api/route", routeRoutes);
app.use("/api/battery", batteryRoutes);

app.use(errorHandler);

module.exports = app;
