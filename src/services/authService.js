const bcrypt = require("bcryptjs");
const { User, UserProfile } = require("../models");
const { generateToken } = require("./jwtService");
const HttpError = require("../utils/httpError");

async function register(request) {
  const existing = await User.findOne({ where: { email: request.email } });
  if (existing) {
    throw new HttpError(400, "Email already taken");
  }

  const user = await User.create({
    email: request.email,
    password: await bcrypt.hash(request.password, 10),
    role: request.role || "USER",
    firstName: request.firstName,
    lastName: request.lastName
  });

  await UserProfile.create({
    userId: user.id,
    name: `${user.firstName || ""} ${user.lastName || ""}`.trim(),
    email: user.email,
    totalTrips: 0,
    totalDistanceKm: 0,
    totalEcoScore: 0,
    totalEnergyUsedKwh: 0,
    totalCo2EmittedKg: 0
  });

  return generateToken(user.email, [user.role]);
}

async function login(request) {
  const user = await User.findOne({ where: { email: request.email } });
  if (!user) {
    throw new HttpError(404, "User not found");
  }

  const ok = await bcrypt.compare(request.password, user.password);
  if (!ok) {
    throw new HttpError(400, "Incorrect password");
  }

  return generateToken(user.email, [user.role]);
}

async function getUserByEmail(email) {
  const user = await User.findOne({ where: { email } });
  if (!user) {
    throw new HttpError(404, "User not found");
  }
  return user;
}

async function getAllUsers() {
  return User.findAll();
}

async function updateUser(id, updatedUser) {
  const user = await User.findByPk(id);
  if (!user) {
    throw new HttpError(404, "User not found");
  }

  user.firstName = updatedUser.firstName;
  user.lastName = updatedUser.lastName;
  user.role = updatedUser.role || user.role;
  await user.save();

  const profile = await UserProfile.findByPk(id);
  if (!profile) {
    throw new HttpError(404, "User profile not found");
  }

  profile.name = `${updatedUser.firstName || ""} ${updatedUser.lastName || ""}`.trim();
  await profile.save();

  return user;
}

async function deleteUser(id) {
  const user = await User.findByPk(id);
  if (!user) {
    throw new HttpError(404, "User not found");
  }

  await UserProfile.destroy({ where: { userId: id } });
  await user.destroy();
}

async function changePassword(userId, oldPassword, newPassword) {
  const user = await User.findByPk(userId);
  if (!user) {
    throw new HttpError(404, "User not found");
  }

  const ok = await bcrypt.compare(oldPassword, user.password);
  if (!ok) {
    throw new HttpError(400, "Old password is incorrect");
  }

  user.password = await bcrypt.hash(newPassword, 10);
  await user.save();
}

async function resetPassword(email, newPassword) {
  const user = await User.findOne({ where: { email } });
  if (!user) {
    throw new HttpError(404, "User not found");
  }

  user.password = await bcrypt.hash(newPassword, 10);
  await user.save();
}

module.exports = {
  register,
  login,
  getUserByEmail,
  getAllUsers,
  updateUser,
  deleteUser,
  changePassword,
  resetPassword
};
