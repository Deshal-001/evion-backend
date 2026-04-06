const express = require("express");
const tripService = require("../services/tripService");
const authenticate = require("../middleware/authenticate");

const router = express.Router();

router.use(authenticate);

router.post("/log", async (req, res, next) => {
  try {
    res.json(await tripService.logTrip(req.body));
  } catch (error) {
    next(error);
  }
});

router.get("/", async (req, res, next) => {
  try {
    res.json(await tripService.getTripHistory(req.query.userId));
  } catch (error) {
    next(error);
  }
});

module.exports = router;
