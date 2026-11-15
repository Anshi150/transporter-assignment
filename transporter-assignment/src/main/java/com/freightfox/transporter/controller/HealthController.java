
package com.freightfox.transporter.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public Map<String, String> health() {

        return Map.of(
                "status", "UP",
                "message",
                "FreightFox Transporter Assignment System is running"
        );
    }
}
