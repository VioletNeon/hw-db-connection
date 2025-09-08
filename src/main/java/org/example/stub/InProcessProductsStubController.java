package org.example.stub;

import org.example.dto.ProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static java.util.List.of;

@RestController
@RequestMapping("/__products_stub/api/products")
public class InProcessProductsStubController {

    private final ProductsStubState state;

    public InProcessProductsStubController(ProductsStubState state) {
        this.state = state;
    }

    // Мини-каталог для ответов 200
    private static final List<ProductDto> CATALOG = of(
            new ProductDto(1L, "ACC-001", new BigDecimal("1234.56"), "ACCOUNT"),
            new ProductDto(2L, "CARD-4111", new BigDecimal("99.99"), "CARD")
    );

    /** Список продуктов пользователя (для совместимости userId игнорируем) */
    @GetMapping
    public List<ProductDto> byUser(@RequestParam(required = false) Long userId) throws InterruptedException {
        behave();
        return CATALOG;
    }

    /** Продукт по id */
    @GetMapping("/{id}")
    public ProductDto byId(@PathVariable Long id) throws InterruptedException {
        behave();
        return CATALOG.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(NotFound::new);
    }

    // ---------- Управление режимами (переключаем курлами) ----------

    /** OK без задержек */
    @PostMapping("/_mode/ok")
    public void ok() { state.setMode(ProductsStubState.Mode.OK); state.setDelayMs(0); }

    /** Медленный ответ (read-timeout → 504; + увидим ретраи/брейкер) */
    @PostMapping("/_mode/slow")
    public void slow(@RequestParam(defaultValue = "10000") int delayMs) {
        state.setMode(ProductsStubState.Mode.SLOW);
        state.setDelayMs(delayMs);
    }

    /** Постоянные 500 (несколько провалов → брейкер откроется → 503) */
    @PostMapping("/_mode/500")
    public void err500() { state.setMode(ProductsStubState.Mode.ERROR500); state.setDelayMs(0); }

    /** 404 (бизнес-ошибка; НЕ ретраим) */
    @PostMapping("/_mode/404")
    public void err404() { state.setMode(ProductsStubState.Mode.NOT_FOUND); state.setDelayMs(0); }

    // ---------- Реакция на текущий режим ----------

    private void behave() throws InterruptedException {
        switch (state.getMode()) {
            case OK -> { /* no-op */ }
            case SLOW -> Thread.sleep(state.getDelayMs());
            case ERROR500 -> throw new InternalError();
            case NOT_FOUND -> throw new NotFound();
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    static class InternalError extends RuntimeException {}
    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFound extends RuntimeException {}
}
