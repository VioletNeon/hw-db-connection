package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentDto(
        @NotNull Long authorId,
        @NotNull Long productId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String externalId
) {}
