package com.gerolori.fasteat.platform.operational;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OperationalStatusController {

    @GetMapping("/health")
    public SignalStatusResponse health() {
        return new SignalStatusResponse("UP");
    }

    @GetMapping("/readiness")
    public SignalStatusResponse readiness() {
        return new SignalStatusResponse("READY");
    }

    public record SignalStatusResponse(String status) {
    }
}
