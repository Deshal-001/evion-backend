package com.evion.evion_backend.batteryEnergySimulation.dto;

import lombok.Data;

@Data
public class BatteryStatusRequest {

    private double batteryCapacityKwh;   // e.g., 60 kWh
    private double currentChargePct;     // e.g., 40 (%)
    private double consumptionPerKm;     // e.g., 0.20 kWh/km
    private double tripDistanceKm;       // distance user wants to travel
    private String drivingMode;          // ECO / NORMAL / AGGRESSIVE
}
