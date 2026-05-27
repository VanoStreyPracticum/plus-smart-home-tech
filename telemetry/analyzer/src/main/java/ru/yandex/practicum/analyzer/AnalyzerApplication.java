package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.analyzer.processor.HubEventProcessor;
import ru.yandex.practicum.analyzer.processor.SnapshotProcessor;

import java.io.File;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApplication {
    public static void main(String[] args) throws Exception {
        // Запускаем hub-router.jar, если он есть в корне проекта
        File hubRouterJar = new File("hub-router.jar");
        if (hubRouterJar.exists()) {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "hub-router.jar", "--server.port=59090");
            pb.inheritIO();
            pb.start();
            // Даём время на старт
            Thread.sleep(5000);
        }

        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);

        HubEventProcessor hubEventProcessor = context.getBean(HubEventProcessor.class);
        SnapshotProcessor snapshotProcessor = context.getBean(SnapshotProcessor.class);

        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        snapshotProcessor.start();
    }
}
