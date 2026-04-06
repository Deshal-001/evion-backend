const express = require("express");
const chargingStationService = require("../services/chargingStationService");
const authenticate = require("../middleware/authenticate");
const authorizeRole = require("../middleware/authorizeRole");

const router = express.Router();

router.get("/", authenticate, async (req, res, next) => {
  try {
    res.json(await chargingStationService.getAllStations());
  } catch (error) {
    next(error);
  }
});

router.get("/:id", authenticate, async (req, res, next) => {
  try {
    res.json(await chargingStationService.getStation(req.params.id));
  } catch (error) {
    next(error);
  }
});

router.post("/", authenticate, authorizeRole("ADMIN"), async (req, res, next) => {
  try {
    res.json(await chargingStationService.addStation(req.body));
  } catch (error) {
    next(error);
  }
});

router.put("/:id", authenticate, authorizeRole("ADMIN"), async (req, res, next) => {
  try {
    res.json(await chargingStationService.updateStation(req.params.id, req.body));
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", authenticate, authorizeRole("ADMIN"), async (req, res, next) => {
  try {
    await chargingStationService.deleteStation(req.params.id);
    res.status(204).send();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
