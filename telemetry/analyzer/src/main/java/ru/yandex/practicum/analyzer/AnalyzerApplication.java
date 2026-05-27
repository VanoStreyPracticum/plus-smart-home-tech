package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.analyzer.processor.SnapshotProcessor;

import java.io.File;
import java.net.Socket;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApplication {
    public static void main(String[] args) throws Exception {
        // 1. Запускаем hub-router.jar, если он есть
        File hubRouterJar = new File("hub-router.jar");
        if (hubRouterJar.exists()) {
            System.out.println("Found hub-router.jar, starting on port 59091...");
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "hub-router.jar", "--server.port=59091");
            pb.inheritIO();
            pb.start();
            // Ждём, пока порт 59091 станет доступен (до 30 секунд)
            waitForPort("localhost", 59091, 30_000);
            System.out.println("hub-router is ready.");
        } else {
            System.err.println("WARNING: hub-router.jar not found in " + System.getProperty("user.dir"));
        }

        // 2. Запускаем Spring-контекст
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);

        // 3. Стартуем обработчики
        HubEventProcessor hubEventProcessor = context.getBean(HubEventProcessor.class);
        SnapshotProcessor snapshotProcessor = context.getBean(SnapshotProcessor.class);

        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        snapshotProcessor.start();
    }

    private static void waitForPort(String host, int port, long timeoutMillis) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMillis) {
            try (Socket s = new Socket(host, port)) {
                return; // порт открыт
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        throw new RuntimeException("Timeout waiting for " + host + ":" + port);
    }
}
