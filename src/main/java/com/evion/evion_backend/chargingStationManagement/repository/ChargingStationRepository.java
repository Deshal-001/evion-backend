package com.evion.evion_backend.chargingStationManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evion.evion_backend.chargingStationManagement.model.ChargingStation;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
}
