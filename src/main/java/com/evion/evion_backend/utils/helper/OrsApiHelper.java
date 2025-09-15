package com.evion.evion_backend.utils.helper;

import com.evion.evion_backend.routeplanner.dto.RouteRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class OrsApiHelper {

    public static JsonNode callOrs(RouteRequest req, String orsDirectionsUrl, String orsApiKey, RestTemplate restTemplate, ObjectMapper mapper) {
        try {
            String url = orsDirectionsUrl + "?api_key=" + orsApiKey;
            Map<String, Object> body = new HashMap<>();
            List<List<Double>> coordinates = Arrays.asList(
                    Arrays.asList(req.getStartLng(), req.getStartLat()),
                    Arrays.asList(req.getEndLng(), req.getEndLat())
            );
            body.put("coordinates", coordinates);

            String response = restTemplate.postForObject(url, body, String.class);
            return mapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call ORS API", e);
        }
    }
}
