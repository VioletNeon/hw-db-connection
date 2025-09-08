package org.example.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.example.domain.Limit;
import org.example.service.LimitService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/limits")
@Validated
public class LimitsController {
    private final LimitService service;

    public LimitsController(LimitService service) {
        this.service = service;
    }

    @GetMapping("/{clientId}/today")
    public Limit today(@PathVariable @NotNull @Min(1) Long clientId) {
        return service.ensure(clientId, LocalDate.now());
    }

    @PutMapping("/reset-now")
    public void resetNow() {
        service.prepareTodayForAllKnownClients();
    }
}
