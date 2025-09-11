package com.evion.evion_backend.routeplanner.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResponse {

    private double distanceKm;
    private double durationSec;
    private double estimatedEnergyKwh;
    private int ecoScore; // 0-100
    private List<Coordinate> polyline; // ordered coords
    private List<Long> suggestedStationIds; // ordered charging stops
    private String message; // status messages
}
