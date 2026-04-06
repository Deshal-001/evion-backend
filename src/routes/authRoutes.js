const express = require("express");
const authService = require("../services/authService");

const router = express.Router();

router.post("/register", async (req, res, next) => {
  try {
    const token = await authService.register(req.body);
    res.json(token);
  } catch (error) {
    next(error);
  }
});

router.post("/login", async (req, res, next) => {
  try {
    const token = await authService.login(req.body);
    res.json(token);
  } catch (error) {
    next(error);
  }
});

router.get("/user/:email", async (req, res, next) => {
  try {
    res.json(await authService.getUserByEmail(req.params.email));
  } catch (error) {
    next(error);
  }
});

router.get("/users", async (req, res, next) => {
  try {
    res.json(await authService.getAllUsers());
  } catch (error) {
    next(error);
  }
});

router.put("/user/:id", async (req, res, next) => {
  try {
    res.json(await authService.updateUser(req.params.id, req.body));
  } catch (error) {
    next(error);
  }
});

router.delete("/user/:id", async (req, res, next) => {
  try {
    await authService.deleteUser(req.params.id);
    res.status(204).send();
  } catch (error) {
    next(error);
  }
});

router.post("/user/:id/change-password", async (req, res, next) => {
  try {
    await authService.changePassword(
      req.params.id,
      req.body.oldPassword,
      req.body.newPassword
    );
    res.json("Password changed successfully");
  } catch (error) {
    next(error);
  }
});

router.post("/forgot-password", async (req, res, next) => {
  try {
    await authService.resetPassword(req.body.email, req.body.newPassword);
    res.json("Password reset successfully");
  } catch (error) {
    next(error);
  }
});

module.exports = router;
