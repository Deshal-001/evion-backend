package com.evion.evion_backend.routeplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;

    // EV info 
    private Double batteryCapacityKwh;    // e.g., 60.0
    private Double currentChargePct;      // 0 - 100
    private Double consumptionPerKm;      // override default (kWh/km)
    private String drivingMode;           // ECO / NORMAL / AGGRESSIVE
}
