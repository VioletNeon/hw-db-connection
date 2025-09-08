# Семинар 16 · Проверка устойчивости (сеть, таймауты, ретраи, circuit breaker)

Ниже — только новое из S16. Для каждого шага: curl, ожидаемый результат, благодаря чему это работает, почему результат такой.

## Предпосылки
• Приложение на http://localhost:8080, профиль dev.
• products.base-url указывает на встроенный стаб: http://localhost:8080/__products_stub/api/products.
• Включены таймауты и Resilience4j (Retry + CircuitBreaker).
• Для удобного вывода установите jq (опционально).

⸻

## 0) Префлайт: сервис и стаб живы

```bash
# Проверка живости сервиса
curl -s http://localhost:8080/actuator/health | jq

# Включаем OK-режим у стаба Products
curl -s -X POST "http://localhost:8080/__products_stub/api/products/_mode/ok" -i
```

**Ожидаем**
• `{"status":"UP"}` от health.
• `HTTP/1.1 200` от _mode/ok.

**Благодаря чему работает**
• Spring Boot Actuator (/actuator/health).
• Встроенный Products-стаб с режимами.

**Почему такой результат**
• Приложение поднято, Actuator сообщает статус.
• Стаб в режиме OK возвращает «здоровые» ответы для следующих сценариев.

⸻

## 1) «Зелёный» путь: всё работает

```bash
EXT="demo-$(date +%s%N)-ok"
curl -s -X POST "http://localhost:8080/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productId\":1,\"amount\":50,\"externalId\":\"${EXT}\"}" | jq
```

**Ожидаем**
HTTP 200 с телом платежа `status: "SUCCESS"`.

**Благодаря чему работает**
• ProductsClient ходит по products.base-url.
• Таймауты заданы, но не срабатывают (ответ быстрый).
• Нет ошибок → Retry/CB не вмешиваются.

**Почему такой результат**
• Стаб в OK-режиме возвращает продукт → PaymentService отрабатывает штатно: идемпотентность пройдена, лимит зарезервирован/подтверждён, платёж SUCCESS.

⸻

## 2) Сетевой таймаут → 504

```bash
# Делаем Products «медленным» (задержка > read-timeout клиента)
curl -s -X POST "http://localhost:8080/__products_stub/api/products/_mode/slow?delayMs=10000"

EXT="demo-$(date +%s%N)-slow"
curl -s -X POST "http://localhost:8080/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productId\":1,\"amount\":20,\"externalId\":\"${EXT}\"}" | jq
```

**Ожидаем**
ProblemDetail 504:

```json
{"title":"Payments upstream error","status":504,"detail":"Upstream timeout or network error"}
```

**Благодаря чему работает**
• RestClient с connect/read таймаутами.
• PaymentsExceptionHandler ловит ResourceAccessException → формирует 504.

**Почему такой результат**
• Стаб намеренно медленный, клиент «протухает» по read-timeout.
• Это не бизнес-ошибка и не 500 сервера, а проблема сети/тайминга, поэтому честный 504.

⸻

## 3) Шторм 500 → Circuit Breaker → 503

```bash
# Включаем постоянные 500 в Products
curl -s -X POST "http://localhost:8080/__products_stub/api/products/_mode/500"

# Несколько вызовов, чтобы набрать порог ошибок
for i in {1..12}; do
  EXT="demo-cb-$(date +%s%N)-$i"
  curl -s -X POST "http://localhost:8080/payments/pay" \
    -H "Content-Type: application/json" \
    -d "{\"userId\":1,\"productId\":1,\"amount\":10,\"externalId\":\"${EXT}\"}" | jq
done
```

**Ожидаем**
Через несколько попыток начнут приходить быстрые 503:

```json
{"title":"Circuit breaker open","status":503,"detail":"Upstream service temporarily unavailable"}
```

**Благодаря чему работает**
• Resilience4j @Retry делает ограниченные повторы при 5xx.
• Resilience4j @CircuitBreaker открывается при высокой доле ошибок (failure-rate-threshold).
• PaymentsExceptionHandler ловит CallNotPermittedException (брейкер открыт) → 503.

**Почему такой результат**
• Постоянные 500 «засоряют» окно CB → он открывается и перестаёт пускать вызовы наружу, мгновенно возвращая 503 (контролируемая деградация).

⸻

## 4) Диагностика устойчивости (Actuator)

```bash
# Состояние брейкера
curl -s http://localhost:8080/actuator/circuitbreakers | jq

# Метрики брейкера (вызовы/ошибки/запрещённые)
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:products" | jq
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:products&tag=state:not_permitted" | jq

# Метрики ретраев
curl -s "http://localhost:8080/actuator/metrics/resilience4j.retry.calls?tag=name:products" | jq
```

**Ожидаем**
• Состояние products = OPEN/HALF_OPEN.
• Счётчики ошибок и not_permitted > 0.

**Благодаря чему работает**
• Spring Boot Actuator + автоэкспорт метрик Resilience4j.

**Почему такой результат**
• Мы реально создали много 5xx → CB открылся; метрики зафиксировали ретраи, ошибки и «запрещённые» вызовы.

⸻

## 5) Восстановление: закрываем брейкер → снова SUCCESS

```bash
# Возвращаем Products в OK
curl -s -X POST "http://localhost:8080/__products_stub/api/products/_mode/ok"

# Ждём, пока CB перейдёт из OPEN в HALF_OPEN (например, 10s)
sleep 11

EXT="demo-$(date +%s%N)-recovered"
curl -s -X POST "http://localhost:8080/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productId\":1,\"amount\":15,\"externalId\":\"${EXT}\"}" | jq
```

**Ожидаем**
Снова HTTP 200 + `status: "SUCCESS"`.

**Благодаря чему работает**
• Resilience4j переводит CB в HALF_OPEN по истечении wait-duration-in-open-state, пробует несколько «пробных» вызовов и закрывает его при успехах.

**Почему такой результат**
• Upstream «починился», CB шаг за шагом вернулся в CLOSED, запросы снова проходят.

⸻

## 6) 404 из Products → прокидываем как есть

```bash
curl -s -X POST "http://localhost:8080/__products_stub/api/products/_mode/404"

EXT="demo-$(date +%s%N)-404"
curl -s -X POST "http://localhost:8080/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productId\":999,\"amount\":10,\"externalId\":\"${EXT}\"}" | jq
```

**Ожидаем**
ProblemDetail 404 (текст зависит от стаба, но статус = 404).

**Благодаря чему работает**
• ProductsClient пробрасывает HttpClientErrorException.
• В конфиге Retry — ignore-exceptions: HttpClientErrorException (4xx не ретраим).
• PaymentsExceptionHandler отдаёт ProblemDetail апстрима как есть.

**Почему такой результат**
• 4xx — клиентская/бизнес-ошибка, не сеть: повторы не нужны, возвращаем честный 404.

⸻

## Если что-то «не так»
• На slow нет 504 → read-timeout-ms должен быть меньше, чем delayMs в стабе.
• На 500 не открывается CB → увеличь число итераций в цикле или понизь failure-rate-threshold/окно sliding-window-size.
• При 404 идут ретраи → в retry.ignore-exceptions должен быть org.springframework.web.client.HttpClientErrorException.
• Все платежи «успех» в любом режиме → проверяй products.base-url: должен указывать на встроенный стаб /__products_stub/api/products.

⸻

## Коротко про "почему всё это работает"
• Таймауты в RestClient дают контролируемый 504 при медленной сети.
• Retry спасает от кратковременных 5xx (но не 4xx и не вечного «slow»).
• Circuit Breaker защищает от шторма ошибок, быстро отдавая 503, пока апстрим болен.
• PaymentsExceptionHandler нормализует ошибки в ProblemDetail, чтобы клиенту было ясно «что случилось».
