package com.nextenti.services.api.rest.health;

import com.nextenti.services.core.dto.NextentiApiResponse;
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
    public NextentiApiResponse<HealthStatusDTO> health() {
        return NextentiApiResponse.success("Application is running", healthService.getStatus());
    }
}
