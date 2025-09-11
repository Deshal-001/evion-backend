package com.evion.evion_backend.routeplanner.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.evion.evion_backend.routeplanner.dto.RouteRequest;
import com.evion.evion_backend.routeplanner.dto.RouteResponse;
import com.evion.evion_backend.routeplanner.service.RouteService;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.planRoute(request));
    }
}
