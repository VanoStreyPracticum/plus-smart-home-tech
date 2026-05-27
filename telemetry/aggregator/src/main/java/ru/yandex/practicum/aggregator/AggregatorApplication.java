package ru.yandex.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class AggregatorApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApplication.class, args);
        // Блокируем основной поток, чтобы приложение не завершалось сразу.
        // В реальном агрегаторе здесь будет запуск цикла опроса Kafka.
        new CountDownLatch(1).await();
    }
}
