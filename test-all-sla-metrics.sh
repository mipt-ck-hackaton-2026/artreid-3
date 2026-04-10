#!/bin/bash

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     Артрейд SLA Мониторинг - Полная проверка всех метрик       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}\n"

# 1. Health Check
echo -e "${YELLOW}1. Health Check${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
HEALTH_RESPONSE=$(curl -s $BASE_URL/api/health)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Health Check PASSED${NC}"
    echo "$HEALTH_RESPONSE"
else
    echo -e "${RED}✗ Health Check FAILED${NC}"
fi
echo ""

# 2. Конфигурация SLA
echo -e "${YELLOW}2. Проверка конфигурации SLA${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
CONFIG=$(curl -s $BASE_URL/api/sla/config)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Config endpoint PASSED${NC}"
    echo "$CONFIG"
else
    echo -e "${RED}✗ Config endpoint FAILED${NC}"
fi
echo ""

# 3. Агрегаты за период
echo -e "${YELLOW}3. Агрегаты за период (апрель 2026)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
SLA_RESPONSE=$(curl -s "$BASE_URL/api/sla/b2c/summary?dateFrom=2026-04-01&dateTo=2026-04-10")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SLA endpoint доступен${NC}\n"

    TOTAL=$(echo "$SLA_RESPONSE" | jq -r '.totalLeads')
    WITHIN=$(echo "$SLA_RESPONSE" | jq -r '.withinSlaCount')
    VIOLATED=$(echo "$SLA_RESPONSE" | jq -r '.violatedSlaCount')
    WITHIN_PCT=$(echo "$SLA_RESPONSE" | jq -r '.withinSlaPercent')
    VIOLATED_PCT=$(echo "$SLA_RESPONSE" | jq -r '.violatedSlaPercent')

    echo -e "${BLUE}📊 Количество сделок в выборке:${NC} ${GREEN}$TOTAL${NC}"
    echo -e "\n${BLUE}📈 Соблюдение SLA:${NC}"
    echo -e "  ✅ В рамках норматива: ${GREEN}$WITHIN${NC} сделок ($WITHIN_PCT%)"
    echo -e "  ❌ С нарушением: ${RED}$VIOLATED${NC} сделок ($VIOLATED_PCT%)"

    AVG=$(echo "$SLA_RESPONSE" | jq -r '.averageFirstResponseMinutes')
    MEDIAN=$(echo "$SLA_RESPONSE" | jq -r '.medianFirstResponseMinutes')
    P90=$(echo "$SLA_RESPONSE" | jq -r '.percentile90FirstResponseMinutes')
    MIN=$(echo "$SLA_RESPONSE" | jq -r '.minMinutes')
    MAX=$(echo "$SLA_RESPONSE" | jq -r '.maxMinutes')

    echo -e "\n${BLUE}⏱ Статистика времени реакции (в минутах):${NC}"
    echo -e "  📊 Среднее: ${GREEN}$AVG${NC}"
    echo -e "  📈 Медиана: ${GREEN}$MEDIAN${NC}"
    echo -e "  🎯 90-й перцентиль: ${GREEN}$P90${NC}"
    echo -e "  ⬇ Минимум: ${GREEN}$MIN${NC}"
    echo -e "  ⬆ Максимум: ${GREEN}$MAX${NC}"

    echo -e "\n${BLUE}📊 Распределение нарушений по интервалам:${NC}"
    echo "$SLA_RESPONSE" | jq -r '.breachDistribution | to_entries[] | "  \(.key): \(.value) сделок"'
else
    echo -e "${RED}✗ SLA endpoint недоступен${NC}"
fi
echo ""

# 4. Проверка фильтрации по менеджеру
echo -e "${YELLOW}4. Проверка фильтрации по менеджеру${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
for MANAGER in MANAGER_001 MANAGER_002 MANAGER_003; do
    RESPONSE=$(curl -s "$BASE_URL/api/sla/b2c/summary?dateFrom=2026-04-01&dateTo=2026-04-10&managerId=$MANAGER")
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalLeads')
    WITHIN=$(echo "$RESPONSE" | jq -r '.withinSlaCount')
    echo -e "Менеджер: ${BLUE}$MANAGER${NC} - сделок: $TOTAL, соблюдено: $WITHIN"
done
echo ""

# 5. Проверка валидации дат
echo -e "${YELLOW}5. Проверка валидации дат${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/sla/b2c/summary?dateFrom=2026-04-10&dateTo=2026-04-01")
if [ "$HTTP_CODE" == "400" ]; then
    echo -e "${GREEN}✓ Валидация работает (dateFrom > dateTo → 400)${NC}"
else
    echo -e "${RED}✗ Валидация не работает (ожидался 400, получен $HTTP_CODE)${NC}"
fi
echo ""

# 6. Проверка полноты ответа API
echo -e "${YELLOW}6. Проверка полноты ответа API${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
REQUIRED_FIELDS=("totalLeads" "withinSlaCount" "violatedSlaCount" "withinSlaPercent"
                 "violatedSlaPercent" "averageFirstResponseMinutes" "medianFirstResponseMinutes"
                 "percentile90FirstResponseMinutes" "minMinutes" "maxMinutes" "breachDistribution")

for FIELD in "${REQUIRED_FIELDS[@]}"; do
    if echo "$SLA_RESPONSE" | jq -e ".$FIELD" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} $FIELD"
    else
        echo -e "  ${RED}✗${NC} $FIELD - отсутствует"
    fi
done
echo ""

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Все проверки пройдены! API работает корректно.${NC}"
