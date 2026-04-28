package org.example.scheduler;

import org.example.service.LimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimitsResetScheduler {
    private static final Logger log = LoggerFactory.getLogger(LimitsResetScheduler.class);
    private final LimitService limitService;

    public LimitsResetScheduler(LimitService limitService) {
        this.limitService = limitService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyLimits() {
        log.info("Resetting daily limits...");

        limitService.prepareTodayForAllKnownClients();

        log.info("Daily limits prepared.");
    }

    // For DEV
    // @Scheduled(cron = "0 * * * * *")
    // public void debugReset() { resetDailyLimits(); }
}

