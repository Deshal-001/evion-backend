package com.evion.evion_backend.tripLogging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.tripLogging.model.Trip;
import com.evion.evion_backend.tripLogging.repository.TripRepository;
import com.evion.evion_backend.userProfile.service.UserProfileService;
import com.evion.evion_backend.vehicle.model.Vehicle;
import com.evion.evion_backend.vehicle.repository.VehicleRepository;
import com.evion.evion_backend.utils.helper.EcoScoreCalculator;
import com.evion.evion_backend.utils.helper.EvionCalculations;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserProfileService userProfileService; // Inject UserProfileService
    private final VehicleRepository vehicleRepository;     // Inject VehicleRepository

    // Save a trip
    public Trip logTrip(
            Long userId,
            double distanceKm,
            double durationSec,
            String drivingMode,
            double startBatteryPct,
            double endBatteryPct,
            List<Long> suggestedStationIds,
            Long vehicleId
    ) {
        // --- fetch vehicle ---
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // --- calculations ---
        double avgSpeed = (durationSec > 0) ? (distanceKm / (durationSec / 3600.0)) : 0.0;
        double energyUsedKwh = EvionCalculations.calculateEnergyUsed(distanceKm, vehicle.getConsumptionPerKm());
        double co2EmittedKg = EvionCalculations.calculateEvCo2(distanceKm, vehicle.getEvEmissionsKgPerKm());
        int ecoScore = EcoScoreCalculator.calculateEcoScore(
                distanceKm,
                energyUsedKwh,
                durationSec,
                drivingMode,
                startBatteryPct,
                endBatteryPct
        );

        // --- create trip ---
        Trip trip = Trip.builder()
                .userId(userId)
                .distanceKm(distanceKm)
                .avgSpeedKmh(avgSpeed)
                .energyUsedKwh(energyUsedKwh)
                .ecoScore(ecoScore)
                .startBatteryPct(startBatteryPct)
                .endBatteryPct(endBatteryPct)
                .tripDate(LocalDateTime.now())
                .suggestedStationIds(suggestedStationIds)
                .co2EmittedKg(co2EmittedKg)
                .vehicleId(vehicleId)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // --- update user profile ---
        userProfileService.recordTrip(userId, distanceKm, ecoScore, energyUsedKwh, co2EmittedKg);

        return savedTrip;
    }

    public List<Trip> getTripHistory(Long userId) {
        return tripRepository.findByUserIdOrderByTripDateDesc(userId);
    }
}
