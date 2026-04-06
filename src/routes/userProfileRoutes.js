const express = require("express");
const userProfileService = require("../services/userProfileService");
const authenticate = require("../middleware/authenticate");

const router = express.Router();

router.use(authenticate);

router.get("/:userId/profile", async (req, res, next) => {
  try {
    res.json(await userProfileService.getUserProfile(req.params.userId));
  } catch (error) {
    next(error);
  }
});

router.put("/:userId/profile", async (req, res, next) => {
  try {
    res.json(await userProfileService.updateUserProfile(req.params.userId, req.body));
  } catch (error) {
    next(error);
  }
});

module.exports = router;
