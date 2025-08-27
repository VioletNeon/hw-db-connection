package org.example.controller;

import org.example.dto.ProductDto;
import org.example.repository.ProductRepository;
import org.example.utils.mappers.ProductMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<ProductDto> getByAuthor(@RequestParam(required = false) Long authorId) {
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "authorId is required");
        }

        return products.findAllByAuthorId(authorId).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductDto getByProductId(@PathVariable Long productId) {
        return products.findById(productId)
                .map(ProductMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }
}

