package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.example.dto.ProductDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ProductsClient {
    private final RestClient rc;

    public ProductsClient(@Qualifier("productsRestClient") RestClient productsRestClient) {
        this.rc = productsRestClient;
    }

    @Retry(name = "products")
    @CircuitBreaker(name = "products")
    public List<ProductDto> findByAuthor(Long authorId) {
        try {
            return rc.get()
                    .uri(uri -> uri.queryParam("authorId", authorId).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProductDto>>() {});
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (RestClientException e) {
            throw e;
        }
    }

    @Retry(name = "products")
    @CircuitBreaker(name = "products")
    public ProductDto findById(Long id) {
        try {
            return rc.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (RestClientException e) {
            throw e;
        }
    }
}
