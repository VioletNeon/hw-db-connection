package org.example.service;

import jakarta.transaction.Transactional;
import org.example.config.LimitsProperties;
import org.example.domain.Limit;
import org.example.exception.LimitExceededException;
import org.example.repository.LimitRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LimitService {
    private final LimitRepository limitRepository;
    private final LimitsProperties props;

    public LimitService(LimitRepository limitRepository, LimitsProperties props) {
        this.limitRepository = limitRepository;
        this.props = props;
    }

    @Transactional
    public Limit ensure(Long clientId, LocalDate day) {
        return limitRepository.findByClientIdAndDay(clientId, day)
                .orElseGet(() -> {
                    Limit l = new Limit();
                    l.setClientId(clientId);
                    l.setDay(day);
                    l.setRemaining(props.getDefaultAmount());

                    return limitRepository.save(l);
                });
    }

    @Transactional
    public Limit tryReserve(Long clientId, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        Limit l = ensure(clientId, today);

        if (l.getRemaining().compareTo(amount) < 0) {
            throw new LimitExceededException("Daily limit exceeded for clientId=" + clientId);
        }

        l.setRemaining(l.getRemaining().subtract(amount));

        return limitRepository.save(l);
    }

    @Transactional
    public Limit restore(Long clientId, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        Limit l = ensure(clientId, today);
        l.setRemaining(l.getRemaining().add(amount));

        return limitRepository.save(l);
    }

    @Transactional
    public void prepareTodayForAllKnownClients() {
        List<Long> clients = limitRepository.findDistinctClientIds();
        LocalDate today = LocalDate.now();

        for (Long clientId : clients) {
            Limit l = limitRepository.findByClientIdAndDay(clientId, today).orElse(null);

            if (l == null) {
                ensure(clientId, today);
            } else {
                l.setRemaining(props.getDefaultAmount());
                limitRepository.save(l);
            }
        }
    }
}

