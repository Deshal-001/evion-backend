package com.evion.evion_backend.batteryEnergySimulation.service;

import org.springframework.stereotype.Service;

import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusRequest;
import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusResponse;

@Service
public class BatterySimulationService {

    private static final double RESERVE_FACTOR = 0.1; // 10% safety margin

    public BatteryStatusResponse simulateBattery(BatteryStatusRequest req) {
        double capacity = req.getBatteryCapacityKwh();
        double currentCharge = capacity * (req.getCurrentChargePct() / 100.0);

        // Adjust consumption based on driving mode
        double adjustedConsumption = adjustConsumption(req.getConsumptionPerKm(), req.getDrivingMode());

        double remainingRange = currentCharge / adjustedConsumption; // km
        double estimatedEnergy = req.getTripDistanceKm() * adjustedConsumption;

        boolean canComplete = remainingRange >= req.getTripDistanceKm() * (1 + RESERVE_FACTOR);

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

    private double adjustConsumption(double baseConsumption, String mode) {
        return switch (mode.toUpperCase()) {
            case "ECO" ->
                baseConsumption * 0.9;
            case "AGGRESSIVE" ->
                baseConsumption * 1.2;
            default ->
                baseConsumption;
        };
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
