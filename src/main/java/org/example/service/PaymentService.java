package org.example.service;

import jakarta.transaction.Transactional;
import org.example.client.ProductsClient;
import org.example.domain.Payment;
import org.example.domain.PaymentStatus;
import org.example.dto.PaymentDto;
import org.example.dto.ProductDto;
import org.example.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProductsClient productsClient;
    private final LimitService limits;

    public PaymentService(
            PaymentRepository paymentRepository,
            ProductsClient productsClient,
            LimitService limits
    ) {
        this.paymentRepository = paymentRepository;
        this.productsClient = productsClient;
        this.limits = limits;
    }

    @Transactional
    public Payment execute(PaymentDto req) {
        if (!StringUtils.hasText(req.externalId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "externalId is required");
        }

        var existing = paymentRepository.findByExternalId(req.externalId());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (req.authorId() == null || req.authorId() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "authorId must be positive");
        }

        if (req.productId() == null || req.productId() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId must be positive");
        }

        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be > 0");
        }

        boolean reserved = false;
        Payment payment = null;

        try {
            limits.tryReserve(req.authorId(), req.amount());
            reserved = true;

            ProductDto product = productsClient.findById(req.productId());

            if (product.balance().compareTo(req.amount()) < 0) {
                limits.restore(req.authorId(), req.amount());
                reserved = false;
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
            }

            payment = new Payment();
            payment.setAuthorId(req.authorId());
            payment.setProductId(req.productId());
            payment.setAmount(req.amount());
            payment.setExternalId(req.externalId());
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);

            payment.setStatus(PaymentStatus.SUCCESS);

            return paymentRepository.save(payment);
        } catch (RuntimeException ex) {
            if (reserved) {
                limits.restore(req.authorId(), req.amount());
            }

            if (payment != null && payment.getId() != null && payment.getStatus() != PaymentStatus.FAILED) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }

            throw ex;
        }

    }

    public Optional<Payment> findById(Long id) {
        if (id == null || id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }

        return paymentRepository.findById(id);
    }

    public List<Payment> findAllByAuthorId(Long authorId) {
        if (authorId == null || authorId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "author id must be positive");
        }

        return paymentRepository.findAllByAuthorId(authorId);
    }

    public List<ProductDto> getProductsByAuthor(Long authorId) {
        return productsClient.findByAuthor(authorId);
    }

    public ProductDto getProductById(Long id) {
        return productsClient.findById(id);
    }
}
