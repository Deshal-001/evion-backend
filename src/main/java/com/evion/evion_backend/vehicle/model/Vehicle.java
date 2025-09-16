package com.evion.evion_backend.vehicle.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Link to user

    private String name;
    private String model;
    private String licensePlate;
    private double consumptionPerKm;

    private double batteryCapacityKwh; // for CO2 & range calculations
    private double evEmissionsKgPerKm; // optional: specific emission factor
}
