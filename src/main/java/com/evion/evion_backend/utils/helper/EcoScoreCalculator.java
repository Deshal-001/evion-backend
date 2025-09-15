package com.evion.evion_backend.utils.helper;

public class EcoScoreCalculator {

    /**
     * Calculates eco score based on trip data. Factors: efficiency, speed
     * discipline, driving mode, battery usage (if available).
     */
    public static int calculateEcoScore(
            double distanceKm,
            double energyKwh,
            double durationSec,
            String drivingMode,
            Double startBatteryPct,
            Double endBatteryPct
    ) {
        if (distanceKm <= 0 || durationSec <= 0 || energyKwh <= 0) {
            return 50; // fallback mid-score
        }

        // --- Efficiency (40%) ---
        double baselineConsumption = 0.20; // kWh/km typical EV
        double actualConsumption = energyKwh / distanceKm;
        double efficiencyScore = (baselineConsumption / actualConsumption) * 100.0;
        efficiencyScore = Math.max(0, Math.min(100, efficiencyScore));

        // --- Speed Discipline (25%) ---
        double avgSpeed = distanceKm / (durationSec / 3600.0);
        double optimalSpeed = 90.0;  // km/h
        double variance = 400.0;     // controls curve steepness
        double speedScore = Math.exp(-Math.pow(avgSpeed - optimalSpeed, 2) / (2 * variance)) * 100.0;

        // --- Driving Mode (20%) ---
        double modeScore = 0;
        if ("ECO".equalsIgnoreCase(drivingMode)) {
            modeScore = 100;
        } else if ("AGGRESSIVE".equalsIgnoreCase(drivingMode)) {
            modeScore = 50;
        } else {
            modeScore = 75;
        }

        // --- Battery Usage (15%) ---
        boolean hasBattery = startBatteryPct != null && endBatteryPct != null && startBatteryPct > 0;
        double batteryScore = 100;
        if (hasBattery) {
            double usedPct = startBatteryPct - endBatteryPct;
            batteryScore = 100 - Math.max(0, usedPct * 2); // penalize high battery drop
        }

        // --- Weighted sum ---
        double finalScore;
        if (hasBattery) {
            finalScore = efficiencyScore * 0.4
                    + speedScore * 0.25
                    + modeScore * 0.2
                    + batteryScore * 0.15;
        } else {
            // Re-normalize weights to sum to 1.0 (Efficiency: 47%, Speed: 29%, Mode: 24%)
            finalScore = efficiencyScore * 0.47
                    + speedScore * 0.29
                    + modeScore * 0.24;
        }

        return (int) Math.max(0, Math.min(100, Math.round(finalScore)));
    }
}
