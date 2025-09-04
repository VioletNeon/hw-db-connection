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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProductsClient productsClient;

    public PaymentService(PaymentRepository paymentRepository, ProductsClient productsClient) {
        this.paymentRepository = paymentRepository;
        this.productsClient = productsClient;
    }

    @Transactional
    public Payment execute(PaymentDto req) {
        if (req.authorId() == null || req.authorId() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "authorId must be positive");
        }

        if (req.productId() == null || req.productId() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId must be positive");
        }

        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be > 0");
        }

        ProductDto product = productsClient.findById(req.productId());

        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        if (product.balance().compareTo(req.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        Payment payment = new Payment();
        payment.setAuthorId(req.authorId());
        payment.setProductId(req.productId());
        payment.setAmount(req.amount());
        payment.setStatus(PaymentStatus.SUCCESS);

        return paymentRepository.save(payment);
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
