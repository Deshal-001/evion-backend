const express = require("express");
const vehicleService = require("../services/vehicleService");
const authenticate = require("../middleware/authenticate");

const router = express.Router();

router.use(authenticate);

router.post("/", async (req, res, next) => {
  try {
    res.json(await vehicleService.createVehicle(req.body));
  } catch (error) {
    next(error);
  }
});

router.get("/user/:userId", async (req, res, next) => {
  try {
    res.json(await vehicleService.getVehiclesByUser(req.params.userId));
  } catch (error) {
    next(error);
  }
});

router.get("/:id", async (req, res, next) => {
  try {
    res.json(await vehicleService.getVehicle(req.params.id));
  } catch (error) {
    next(error);
  }
});

router.put("/:id", async (req, res, next) => {
  try {
    res.json(await vehicleService.updateVehicle(req.params.id, req.body));
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", async (req, res, next) => {
  try {
    await vehicleService.deleteVehicle(req.params.id);
    res.status(204).send();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
