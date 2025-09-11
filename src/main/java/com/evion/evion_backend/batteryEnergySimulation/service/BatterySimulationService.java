package com.evion.evion_backend.batteryEnergySimulation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusRequest;
import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusResponse;

@Service
public class BatterySimulationService {

    @Value("${ev.default.battery.kwh}")
    private double defaultBatteryKwh;

    @Value("${ev.reserve.factor}")
    private double reserveFactor;

    // Selects energy consumption per km based on driving mode or overrides
    public double selectConsumption(String drivingMode, Double overrideConsumption,
            double ecoConsumption, double normalConsumption, double aggressiveConsumption) {
        if (overrideConsumption != null && overrideConsumption > 0) {
            return overrideConsumption;
        }
        if (drivingMode == null) {
            return normalConsumption;
        }
        return switch (drivingMode.toUpperCase()) {
            case "ECO" ->
                ecoConsumption;
            case "AGGRESSIVE" ->
                aggressiveConsumption;
            default ->
                normalConsumption;
        };
    }

    // Computes usable range in km from battery capacity & charge
    public double computeUsableRangeKm(Double batteryKwh, Double currentPct, double consumptionPerKm, double reserveFactor) {
        if (batteryKwh == null || currentPct == null || consumptionPerKm <= 0) {
            return Double.MAX_VALUE; // fallback if missing info
        }
        double energyAvailableKwh = (currentPct / 100.0) * batteryKwh;
        double usableRange = energyAvailableKwh / consumptionPerKm;
        return usableRange * (1.0 - reserveFactor);
    }

    // Fallback if battery info missing
    public double batteryOrFallback(Double batteryKwh) {
        return (batteryKwh != null && batteryKwh > 0) ? batteryKwh : defaultBatteryKwh;
    }

    public BatteryStatusResponse simulateBattery(BatteryStatusRequest req) {
        double capacity = req.getBatteryCapacityKwh();
        double currentCharge = capacity * (req.getCurrentChargePct() / 100.0);

        // Adjust consumption based on driving mode
        double adjustedConsumption = adjustConsumption(req.getConsumptionPerKm(), req.getDrivingMode());

        double remainingRange = currentCharge / adjustedConsumption; // km
        double estimatedEnergy = req.getTripDistanceKm() * adjustedConsumption;

        boolean canComplete = remainingRange >= req.getTripDistanceKm() * (1 + reserveFactor);

        String alert = null;
        if (!canComplete) {
            alert = "Insufficient range. Please plan a charging stop.";
        } else if (req.getCurrentChargePct() < 20) {
            alert = "Warning: Battery below 20%. Consider charging soon.";
        }

        return BatteryStatusResponse.builder()
                .remainingRangeKm(round(remainingRange))
                .estimatedEnergyUse(round(estimatedEnergy))
                .canCompleteTrip(canComplete)
                .alertMessage(alert)
                .build();
    }

    private double adjustConsumption(double baseConsumption, String drivingMode) {
        if (drivingMode == null) {
            return baseConsumption;
        }
        return switch (drivingMode.toUpperCase()) {
            case "ECO" ->
                baseConsumption * 0.85;
            case "AGGRESSIVE" ->
                baseConsumption * 1.15;
            default ->
                baseConsumption;
        };
    }

// Rounds a double to 2 decimal places
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
