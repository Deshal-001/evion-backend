const express = require("express");
const routeService = require("../services/routeService");

const router = express.Router();

router.post("/plan", async (req, res, next) => {
  try {
    res.json(await routeService.planRoute(req.body));
  } catch (error) {
    next(error);
  }
});

module.exports = router;
