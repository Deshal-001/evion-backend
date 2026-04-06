const { UserProfile } = require("../models");
const HttpError = require("../utils/httpError");

async function getUserProfile(userId) {
  const profile = await UserProfile.findByPk(userId);
  if (!profile) {
    throw new HttpError(404, "User not found");
  }

  const averageEcoScore =
    profile.totalTrips > 0 ? profile.totalEcoScore / profile.totalTrips : 0;

  return {
    userId: profile.userId,
    name: profile.name,
    email: profile.email,
    totalTrips: profile.totalTrips,
    totalDistanceKm: profile.totalDistanceKm,
    averageEcoScore,
    totalCo2EmittedKg: profile.totalCo2EmittedKg
  };
}

async function updateUserProfile(userId, request) {
  const profile = await UserProfile.findByPk(userId);
  if (!profile) {
    throw new HttpError(404, "User not found");
  }

  profile.name = request.name;
  await profile.save();

  return getUserProfile(userId);
}

async function recordTrip(userId, distanceKm, ecoScore, energyUsedKwh, co2EmittedKg) {
  const profile = await UserProfile.findByPk(userId);
  if (!profile) {
    throw new HttpError(404, "User not found");
  }

  profile.totalTrips += 1;
  profile.totalDistanceKm += distanceKm;
  profile.totalEcoScore += ecoScore;
  profile.totalEnergyUsedKwh += energyUsedKwh;
  profile.totalCo2EmittedKg += co2EmittedKg;

  await profile.save();
}

module.exports = {
  getUserProfile,
  updateUserProfile,
  recordTrip
};
