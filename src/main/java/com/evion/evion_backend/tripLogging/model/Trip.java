package com.evion.evion_backend.tripLogging.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private double distanceKm;
    private double avgSpeedKmh;
    private double energyUsedKwh;
    private int ecoScore;
    private double startBatteryPct;
    private double endBatteryPct;
    private List<Long> suggestedStationIds;
    private double co2SavedKg;

    private LocalDateTime tripDate;
}
