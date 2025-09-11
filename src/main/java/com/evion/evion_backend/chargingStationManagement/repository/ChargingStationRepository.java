package com.evion.evion_backend.chargingstationmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evion.evion_backend.chargingstationmanagement.model.ChargingStation;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
}
