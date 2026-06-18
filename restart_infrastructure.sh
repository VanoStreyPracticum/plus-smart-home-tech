#!/usr/bin/env bash
set -e

# === Настройка JDK 21 ===
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
echo "Using JAVA_HOME=$JAVA_HOME"
java -version

# === 1. Останавливаем все spring-boot процессы ===
pkill -f 'spring-boot:run' || true
sleep 2

# === 2. Останавливаем и удаляем Docker контейнеры с очисткой томов (БД сбросится) ===
docker compose down -v 2>/dev/null || true
sleep 2

# === 3. Убеждаемся, что compose.yaml содержит hub-router (на порту 59090) и postgres ===
cat << 'EOF' > compose.yaml
services:
  kafka:
    image: confluentinc/confluent-local:7.4.3
    hostname: kafka
    container_name: kafka
    ports:
      - "9092:9092"
      - "9101:9101"
    restart: unless-stopped
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092'
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:29093'
      KAFKA_LISTENERS: 'PLAINTEXT://kafka:29092,CONTROLLER://kafka:29093,PLAINTEXT_HOST://0.0.0.0:9092'
      CLUSTER_ID: 'K0EA9p0yEe6MkAAAAkKsEg'

  kafka-init-topics:
    image: confluentinc/confluent-local:7.4.3
    container_name: kafka-init-topics
    depends_on:
      - kafka
    command: "bash -c \
                'kafka-topics --create --topic telemetry.sensors.v1 \
                             --partitions 1 --replication-factor 1 --if-not-exists \
                             --bootstrap-server kafka:29092 && \
                kafka-topics --create --topic telemetry.snapshots.v1 \
                             --partitions 1 --replication-factor 1 --if-not-exists \
                             --bootstrap-server kafka:29092 && \
                kafka-topics --create --topic telemetry.hubs.v1 \
                             --partitions 1 --replication-factor 1 --if-not-exists \
                             --bootstrap-server kafka:29092'"
    init: true

  postgres:
    image: postgres:16.1
    container_name: postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_DB: smart_home
    restart: unless-stopped

  hub-router:
    image: eclipse-temurin:21-jre
    container_name: hub-router
    depends_on:
      - kafka
    volumes:
      - ./hub-router/scripts/hub-router.jar:/app/hub-router.jar
    working_dir: /app
    command: ["java", "-jar", "hub-router.jar", "--server.port=59090"]
    ports:
      - "59090:59090"
EOF

# === 4. Запускаем инфраструктуру ===
echo "🚀 Запуск Docker Compose..."
docker compose up -d

# === 5. Ожидание готовности сервисов ===
echo "⏳ Ожидание Kafka..."
until docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 &>/dev/null; do sleep 2; done
echo "✅ Kafka готов"

echo "⏳ Ожидание PostgreSQL..."
until docker exec postgres pg_isready -U postgres &>/dev/null; do sleep 2; done
echo "✅ PostgreSQL готов"

echo "⏳ Ожидание hub-router (порт 59090)..."
until curl -s -o /dev/null -w '' http://localhost:59090; do sleep 2; done
echo "✅ hub-router готов"

# === 6. Сборка всех модулей (чтобы JAR'ы были актуальными) ===
echo "🔧 Сборка проекта..."
mvn clean install -DskipTests

# === 7. Запуск трёх сервисов в фоне с записью логов ===
echo "🚀 Запуск Collector..."
nohup mvn -pl telemetry/collector spring-boot:run > logs/collector.log 2>&1 &
echo $! > logs/collector.pid

echo "🚀 Запуск Aggregator..."
nohup mvn -pl telemetry/aggregator spring-boot:run > logs/aggregator.log 2>&1 &
echo $! > logs/aggregator.pid

echo "🚀 Запуск Analyzer..."
nohup mvn -pl telemetry/analyzer spring-boot:run > logs/analyzer.log 2>&1 &
echo $! > logs/analyzer.pid

# === 8. Ожидание старта сервисов (проверяем порт Collector'а и появление строки Started в логах) ===
echo "⏳ Ожидание Collector (порт 8080)..."
until curl -s -o /dev/null http://localhost:8080/events/sensors; do sleep 2; done
echo "✅ Collector готов"

echo "⏳ Ожидание Aggregator..."
until grep -q "Started AggregatorApplication" logs/aggregator.log 2>/dev/null; do sleep 2; done
echo "✅ Aggregator готов"

echo "⏳ Ожидание Analyzer..."
until grep -q "Started AnalyzerApplication" logs/analyzer.log 2>/dev/null; do sleep 2; done
echo "✅ Analyzer готов"

# === 9. Инициализация тестового сценария в БД ===
echo "📦 Вставка тестового сценария в PostgreSQL..."
echo "
INSERT INTO sensors (id, hub_id) VALUES ('sensor-1', 'hub-1');
INSERT INTO conditions (type, operation, value) VALUES ('TEMPERATURE', 'GREATER_THAN', 25);
INSERT INTO actions (type, value) VALUES ('SET_VALUE', 20);
INSERT INTO scenarios (hub_id, name) VALUES ('hub-1', 'Жарко – включить кондиционер');
INSERT INTO scenario_conditions (scenario_id, sensor_id, condition_id) VALUES (1, 'sensor-1', 1);
INSERT INTO scenario_actions (scenario_id, sensor_id, action_id) VALUES (1, 'sensor-1', 1);
" | docker exec -i postgres psql -U postgres -d smart_home

# === 10. Отправка тестового события ===
echo "📤 Отправка тестового события датчика..."
curl -X POST http://localhost:8080/events/sensors \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "TEMPERATURE_SENSOR_EVENT",
    "id": "sensor-1",
    "hubId": "hub-1",
    "temperatureC": 30,
    "temperatureF": 86
  }'

echo ""
echo "🎉 Инфраструктура запущена. Логи сервисов:"
echo "   Collector : tail -f logs/collector.log"
echo "   Aggregator: tail -f logs/aggregator.log"
echo "   Analyzer  : tail -f logs/analyzer.log"
echo "   Hub Router: docker logs -f hub-router"
echo ""
echo "Ожидаемые сообщения в логах Analyzer:"
echo "  'Processing snapshot for hub hub-1'"
echo "  'Executing scenario 'Жарко – включить кондиционер' for hub hub-1'"
echo "  'Sent action to hub-router'"