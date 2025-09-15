package com.evion.evion_backend.tripLogging.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripRequest {

    private Long userId;
    private double distanceKm;
    private double durationSec;
    private double energyUsedKwh;
    private String drivingMode; // ECO, NORMAL, AGGRESSIVE
    private Double startBatteryPct;
    private Double endBatteryPct;
    private List<Long> suggestedStationIds;
}
