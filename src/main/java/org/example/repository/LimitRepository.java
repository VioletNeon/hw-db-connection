package org.example.repository;

import org.example.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Long> {
    Optional<Limit> findByClientIdAndDay(Long clientId, LocalDate day);

    @Query("select distinct l.clientId from Limit l")
    List<Long> findDistinctClientIds();
}
