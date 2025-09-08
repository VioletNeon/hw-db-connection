package org.example.stub;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Конфигурация WireMock для эмуляции внешнего Products сервиса.
 * 
 * ВАЖНО: WireMock конфигурация временно отключена из-за проблем с компиляцией.
 * Для демонстрации используйте внешний WireMock сервер или curl команды из демо-скрипта.
 */
@Configuration
@Profile("dev")
public class ProductsWireMockConfig {

    @Value("${products.base-url:http://localhost:8090}")
    private String productsBaseUrl;

    @PostConstruct
    public void startWireMock() {
        System.out.println("WireMock конфигурация загружена. URL: " + productsBaseUrl);
        System.out.println("Для демонстрации запустите WireMock отдельно:");
        System.out.println("java -jar wiremock-standalone.jar --port 8090");
    }

    @PreDestroy
    public void stopWireMock() {
        System.out.println("WireMock конфигурация выгружена");
    }
}
