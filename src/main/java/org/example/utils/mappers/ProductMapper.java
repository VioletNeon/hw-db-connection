package org.example.utils.mappers;

import org.example.domain.Product;
import org.example.dto.ProductDto;

public final class ProductMapper {
    private ProductMapper() {}

    public static ProductDto toDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getAccountNumber(),
                p.getBalance(),
                p.getType()
        );
    }
}
