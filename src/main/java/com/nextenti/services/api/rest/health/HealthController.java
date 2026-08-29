package com.nextenti.services.api.rest.health;

import com.nextenti.services.core.dto.SmartRoadResponseDTO;
import com.nextenti.services.core.dto.health.HealthStatusDTO;
import com.nextenti.services.core.service.health.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public SmartRoadResponseDTO<HealthStatusDTO> health() {
        return SmartRoadResponseDTO.success("Application is running", healthService.getStatus());
    }
}
