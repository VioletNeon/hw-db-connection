package org.example.client;


import org.example.dto.ProductDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ProductsClient {
    private final RestClient rc;

    public ProductsClient(RestClient productsRestClient) {
        this.rc = productsRestClient;
    }

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

