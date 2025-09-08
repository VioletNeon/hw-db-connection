package org.example.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ProductsProperties.class)
public class HttpClientsConfig {

    @Bean
    public RestClient productsRestClient(ProductsProperties props) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getHttp().getConnectTimeoutMs());
        factory.setReadTimeout(props.getHttp().getReadTimeoutMs());

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(props.getProductsBaseUrl())
                .build();
    }
}

