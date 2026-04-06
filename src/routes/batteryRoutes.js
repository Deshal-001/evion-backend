const express = require("express");
const batteryService = require("../services/batterySimulationService");

const router = express.Router();

router.post("/status", (req, res, next) => {
  try {
    res.json(batteryService.simulateBattery(req.body));
  } catch (error) {
    next(error);
  }
});

module.exports = router;
