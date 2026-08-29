package com.nextenti.services.core.service.health;

import com.nextenti.services.core.dto.health.HealthStatusDTO;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthStatusDTO getStatus() {
        return new HealthStatusDTO("UP");
    }
}
