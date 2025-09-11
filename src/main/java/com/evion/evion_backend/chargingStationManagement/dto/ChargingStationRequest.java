package com.evion.evion_backend.chargingstationmanagement.dto;

import com.evion.evion_backend.chargingstationmanagement.model.ChargingStation;

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
