package com.evion.evion_backend.chargingStationManagement.dto;

import com.evion.evion_backend.chargingStationManagement.model.ChargingStation;

import lombok.Data;

@Data
public class ChargingStationRequest {

    private String name;
    private double latitude;
    private double longitude;
    private String chargingType;
    private double chargingSpeed;
    private double costPerKwh;
    private ChargingStation.Status status;
}
