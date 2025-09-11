package com.evion.evion_backend.chargingstationmanagement.dto;

import com.evion.evion_backend.chargingstationmanagement.model.ChargingStation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChargingStationResponse {

    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private String chargingType;
    private double chargingSpeed;
    private double costPerKwh;
    private ChargingStation.Status status;
}
