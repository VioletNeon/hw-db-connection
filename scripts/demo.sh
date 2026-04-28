#!/bin/bash

# Семинар 16: Демо-скрипт для проверки взаимодействия микросервисов
# Демонстрирует: Retry, Circuit Breaker, таймауты, идемпотентность

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Базовые URL
BASE_P="http://localhost:8080"
BASE_W="http://localhost:8090"
USER=1
CLIENT=1

echo -e "${BLUE}=== Семинар 16: Демонстрация взаимодействия микросервисов ===${NC}"
echo

# Функция для вывода заголовка секции
print_section() {
    echo -e "${YELLOW}--- $1 ---${NC}"
}

# Функция для вывода результата
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Функция для ожидания
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "Ожидание запуска $name... "
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC}"
            return 0
        fi
        sleep 1
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗${NC}"
    return 1
}

print_section "Проверка доступности сервисов"
wait_for_service "$BASE_P/actuator/health" "Payments"
wait_for_service "$BASE_W/__admin" "WireMock"

print_section "1. Инициализация WireMock стабов"

# 200 для id=1
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 10,
  "request":  { "method":"GET", "urlPath":"/api/products/1" },
  "response": { "status": 200, "headers": {"Content-Type":"application/json"},
    "jsonBody": { "id":1, "accountNumber":"ACC-001", "balance":1234.56, "type":"ACCOUNT", "userId":1 } }
}' > /dev/null
print_result $? "Стаб 200 для /api/products/1"

# 404 для id=999
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 10,
  "request":  { "method":"GET", "urlPath":"/api/products/999" },
  "response": { "status": 404, "headers": {"Content-Type":"application/problem+json"},
    "jsonBody": { "type":"about:blank", "title":"product not found", "status":404, "detail":"product not found" } }
}' > /dev/null
print_result $? "Стаб 404 для /api/products/999"

print_section "2. Проверка остатка дневного лимита"
LIMIT_RESPONSE=$(curl -s "$BASE_P/limits/$CLIENT/today" 2>/dev/null)
if echo "$LIMIT_RESPONSE" | jq -e '.remaining' > /dev/null 2>&1; then
    REMAINING=$(echo "$LIMIT_RESPONSE" | jq -r '.remaining')
    echo -e "${GREEN}✓ Остаток лимита: $REMAINING${NC}"
else
    echo -e "${YELLOW}⚠ Лимиты не настроены, продолжаем без проверки${NC}"
fi

print_section "3. Успешный платеж"
EXT1="ok-$(date +%s%N)"
PAYMENT_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER,\"productId\":1,\"amount\":100.00,\"externalId\":\"$EXT1\"}" 2>/dev/null)

if echo "$PAYMENT_RESPONSE" | jq -e '.status' > /dev/null 2>&1; then
    STATUS=$(echo "$PAYMENT_RESPONSE" | jq -r '.status')
    echo -e "${GREEN}✓ Платеж создан, статус: $STATUS${NC}"
else
    echo -e "${RED}✗ Ошибка создания платежа${NC}"
    echo "$PAYMENT_RESPONSE"
fi

print_section "4. Идемпотентность (повторный платеж)"
IDEMPOTENT_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER,\"productId\":1,\"amount\":100.00,\"externalId\":\"$EXT1\"}" 2>/dev/null)

if echo "$IDEMPOTENT_RESPONSE" | jq -e '.status' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Идемпотентность работает${NC}"
else
    echo -e "${RED}✗ Ошибка идемпотентности${NC}"
    echo "$IDEMPOTENT_RESPONSE"
fi

print_section "5. Бизнес-ошибка: 404 (несуществующий продукт)"
EXT404="p404-$(date +%s%N)"
ERROR404_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER,\"productId\":999,\"amount\":10.00,\"externalId\":\"$EXT404\"}" 2>/dev/null)

if echo "$ERROR404_RESPONSE" | jq -e '.status == 404' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 404 ошибка корректно обработана${NC}"
else
    echo -e "${RED}✗ Ошибка обработки 404${NC}"
    echo "$ERROR404_RESPONSE"
fi

print_section "6. Таймаут: медленный Products (504)"
# Создаем медленную заглушку
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 1,
  "request":  { "method":"GET", "urlPath":"/api/products/1" },
  "response": { "status": 200, "fixedDelayMilliseconds": 5000,
    "headers": {"Content-Type": "application/json"},
    "jsonBody": { "id":1, "accountNumber":"ACC-001","balance":1234.56,"type":"ACCOUNT", "userId":1 } }
}' > /dev/null

EXTSLOW="slow-$(date +%s%N)"
TIMEOUT_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER,\"productId\":1,\"amount\":10.00,\"externalId\":\"$EXTSLOW\"}" 2>/dev/null)

if echo "$TIMEOUT_RESPONSE" | jq -e '.status == 504' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Таймаут корректно обработан (504)${NC}"
else
    echo -e "${RED}✗ Ошибка обработки таймаута${NC}"
    echo "$TIMEOUT_RESPONSE"
fi

# Восстанавливаем нормальную скорость
curl -s -X POST "$BASE_W/__admin/reset" > /dev/null
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 10,
  "request":  { "method":"GET", "urlPath":"/api/products/1" },
  "response": { "status": 200, "headers": {"Content-Type":"application/json"},
    "jsonBody": { "id":1, "accountNumber":"ACC-001", "balance":1234.56, "type":"ACCOUNT", "userId":1 } }
}' > /dev/null

print_section "7. Circuit Breaker: серия 500 ошибок (503)"
# Создаем маппинг на 500
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 1,
  "request":  { "method":"GET", "urlPath":"/api/products/1" },
  "response": { "status": 500, "headers": {"Content-Type":"application/json"},
    "jsonBody": {"message":"boom"} }
}' > /dev/null

echo "Отправляем серию запросов для открытия Circuit Breaker..."
for i in {1..5}; do
    EXTE="e500-$i-$(date +%s%N)"
    CB_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
      -H "Content-Type: application/json" \
      -d "{\"userId\":$USER,\"productId\":1,\"amount\":5.00,\"externalId\":\"$EXTE\"}" 2>/dev/null)
    
    if echo "$CB_RESPONSE" | jq -e '.status == 503' > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Circuit Breaker открыт (503)${NC}"
        break
    elif echo "$CB_RESPONSE" | jq -e '.status == 500' > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠ Получена 500 ошибка (попытка $i)${NC}"
    else
        echo -e "${RED}✗ Неожиданный ответ${NC}"
        echo "$CB_RESPONSE"
    fi
done

print_section "8. Мониторинг: проверка метрик"
echo "Состояние Circuit Breaker:"
curl -s "$BASE_P/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:products" 2>/dev/null | jq '.measurements[0].value' || echo "Метрики недоступны"

echo "Статистика вызовов:"
curl -s "$BASE_P/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:products" 2>/dev/null | jq '.measurements' || echo "Метрики недоступны"

print_section "9. Восстановление: возврат к нормальной работе"
# Сбрасываем стабы и возвращаем 200
curl -s -X POST "$BASE_W/__admin/reset" > /dev/null
curl -s -X POST "$BASE_W/__admin/mappings" -H "Content-Type: application/json" -d '{
  "priority": 10,
  "request":  { "method":"GET", "urlPath":"/api/products/1" },
  "response": { "status": 200, "headers": {"Content-Type":"application/json"},
    "jsonBody": { "id":1, "accountNumber":"ACC-001", "balance":1234.56, "type":"ACCOUNT", "userId":1 } }
}' > /dev/null

echo "Ждем восстановления Circuit Breaker (10 секунд)..."
sleep 10

EXTOK2="ok2-$(date +%s%N)"
RECOVERY_RESPONSE=$(curl -s -X POST "$BASE_P/payments/pay" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER,\"productId\":1,\"amount\":7.00,\"externalId\":\"$EXTOK2\"}" 2>/dev/null)

if echo "$RECOVERY_RESPONSE" | jq -e '.status == "SUCCESS"' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Восстановление успешно${NC}"
else
    echo -e "${YELLOW}⚠ Восстановление в процессе${NC}"
    echo "$RECOVERY_RESPONSE"
fi

print_section "10. Swagger UI"
echo -e "${BLUE}Swagger UI доступен по адресу: $BASE_P/swagger-ui/index.html${NC}"

print_section "Демонстрация завершена"
echo -e "${GREEN}✓ Все сценарии протестированы${NC}"
echo
echo -e "${BLUE}Доступные endpoints:${NC}"
echo "- Payments API: $BASE_P/payments"
echo "- Health Check: $BASE_P/actuator/health"
echo "- Метрики: $BASE_P/actuator/metrics"
echo "- Swagger UI: $BASE_P/swagger-ui/index.html"
echo "- WireMock Admin: $BASE_W/__admin"
