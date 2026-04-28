package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payments Service API")
                        .description("""
                                ### Основные возможности:
                                - **Платежи**: создание, просмотр, поиск по пользователю
                                - **Лимиты**: дневные лимиты с автоматическим сбросом
                                - **Устойчивость**: Retry и Circuit Breaker для внешних вызовов
                                - **Мониторинг**: Actuator endpoints для health checks и метрик
                                
                                ### Сценарии тестирования:
                                1. **Успешный платеж** - обычный сценарий
                                2. **Идемпотентность** - повторный платеж с тем же externalId
                                3. **404 ошибка** - несуществующий продукт
                                4. **Таймаут** - медленный ответ от Products (504)
                                5. **Circuit Breaker** - серия 500 ошибок (503)
                                6. **Восстановление** - возврат к нормальной работе
                                
                                ### Мониторинг:
                                - Health: `/actuator/health`
                                - Метрики: `/actuator/metrics`
                                - Circuit Breaker: `/actuator/circuitbreakers`
                                - Retry: `/actuator/retries`
                                """)
                        .version("1.0.0")
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}
