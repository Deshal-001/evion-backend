package com.evion.evion_backend.vehicle.repository;

import com.evion.evion_backend.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByUserId(Long userId);
}
