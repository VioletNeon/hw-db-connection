package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentDto(
        @NotNull Long authorId,
        @NotNull Long productId,
        @NotNull @Min(1) BigDecimal amount
) {}
