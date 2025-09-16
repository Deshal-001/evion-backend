package com.evion.evion_backend.tripLogging.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evion.evion_backend.tripLogging.model.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserIdOrderByTripDateDesc(Long userId);

    List<Trip> findByUserId(Long userId);

    List<Trip> findAllByVehicleId(Long vehicleId);

}
