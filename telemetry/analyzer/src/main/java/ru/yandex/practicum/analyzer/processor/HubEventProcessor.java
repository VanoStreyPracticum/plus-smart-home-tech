package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
public class HubEventProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);
    private final Consumer<String, byte[]> consumer;

    public HubEventProcessor(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-hub-events-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        this.consumer = new KafkaConsumer<>(props);
    }

    @Override
    public void run() {
        consumer.subscribe(List.of("telemetry.hubs.v1"));
        while (!Thread.currentThread().isInterrupted()) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            records.forEach(record -> {
                try {
                    HubEventAvro event = deserializeAvro(record.value(), HubEventAvro.class);
                    log.info("Received hub event: {}", event);
                } catch (Exception e) {
                    log.error("Error processing hub event", e);
                }
            });
        }
    }

    private <T> T deserializeAvro(byte[] data, Class<T> clazz) throws Exception {
        var reader = new SpecificDatumReader<>(clazz);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);
        return clazz.cast(reader.read(null, decoder));
    }

    @PreDestroy
    public void shutdown() {
        consumer.close();
    }
}
