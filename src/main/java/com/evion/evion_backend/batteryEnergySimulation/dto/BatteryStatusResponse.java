package com.evion.evion_backend.batteryEnergySimulation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryStatusResponse {

    private double remainingRangeKm;     // how far can go
    private double estimatedEnergyUse;   // energy needed for trip
    private boolean canCompleteTrip;     // enough battery or not
    private String alertMessage;         // warning if low range
}
