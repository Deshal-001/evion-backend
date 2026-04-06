function calculateEcoScore(
  distanceKm,
  energyKwh,
  durationSec,
  drivingMode,
  startBatteryPct,
  endBatteryPct
) {
  if (distanceKm <= 0 || durationSec <= 0 || energyKwh <= 0) {
    return 50;
  }

  const baselineConsumption = 0.2;
  const actualConsumption = energyKwh / distanceKm;
  let efficiencyScore = (baselineConsumption / actualConsumption) * 100;
  efficiencyScore = Math.max(0, Math.min(100, efficiencyScore));

  const avgSpeed = distanceKm / (durationSec / 3600);
  const optimalSpeed = 90;
  const variance = 400;
  const speedScore =
    Math.exp(-Math.pow(avgSpeed - optimalSpeed, 2) / (2 * variance)) * 100;

  let modeScore = 75;
  if ((drivingMode || "").toUpperCase() === "ECO") {
    modeScore = 100;
  } else if ((drivingMode || "").toUpperCase() === "AGGRESSIVE") {
    modeScore = 50;
  }

  const hasBattery =
    startBatteryPct != null && endBatteryPct != null && startBatteryPct > 0;

  let finalScore;
  if (hasBattery) {
    const usedPct = startBatteryPct - endBatteryPct;
    const batteryScore = 100 - Math.max(0, usedPct * 2);
    finalScore =
      efficiencyScore * 0.4 +
      speedScore * 0.25 +
      modeScore * 0.2 +
      batteryScore * 0.15;
  } else {
    finalScore =
      efficiencyScore * 0.47 + speedScore * 0.29 + modeScore * 0.24;
  }

  return Math.max(0, Math.min(100, Math.round(finalScore)));
}

module.exports = {
  calculateEcoScore
};
