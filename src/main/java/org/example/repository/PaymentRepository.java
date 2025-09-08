package org.example.repository;

import org.example.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByAuthorId(Long authorId);
    Optional<Payment> findByExternalId(String externalId);
}
