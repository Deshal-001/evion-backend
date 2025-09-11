package com.evion.evion_backend.chargingStationManagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "charging_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double latitude;
    private double longitude;

    private String chargingType; // e.g., AC, DC Fast, Supercharger
    private double chargingSpeed; // in kW
    private double costPerKwh;   // in EUR

    @Enumerated(EnumType.STRING)
    private Status status; // AVAILABLE, OCCUPIED, OUT_OF_SERVICE

    public enum Status {
        AVAILABLE, OCCUPIED, OUT_OF_SERVICE
    }
}
