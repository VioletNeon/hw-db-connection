package org.example.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.domain.Payment;
import org.example.dto.PaymentDto;
import org.example.dto.ProductDto;
import org.example.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    @ResponseStatus(HttpStatus.CREATED)
    public Payment doPayment(@RequestBody PaymentDto paymentDto) {
        return paymentService.execute(paymentDto);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable @NotNull @Min(1) Long id) {
        return paymentService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @GetMapping("/by-author")
    public List<Payment> getPaymentByAuthorId(@RequestParam @NotNull @Min(1) Long authorId) {
        return paymentService.findAllByAuthorId(authorId);
    }

    @GetMapping("/products")
    public List<ProductDto> productsByAuthor(@RequestParam @NotNull @Min(1) Long authorId) {
        return paymentService.getProductsByAuthor(authorId);
    }

    @GetMapping("/products/{id}")
    public ProductDto productById(@PathVariable @NotNull @Min(1) Long id) {
        return paymentService.getProductById(id);
    }

}
