package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.ScenarioAnalyzer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final ScenarioAnalyzer analyzer;
    private Consumer<String, byte[]> consumer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public void start() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-snapshots-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        this.consumer = new KafkaConsumer<>(props);

        consumer.subscribe(List.of("telemetry.snapshots.v1"));
        while (!Thread.currentThread().isInterrupted()) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            records.forEach(record -> {
                try {
                    SensorEventAvro snapshot = deserializeAvro(record.value(), SensorEventAvro.class);
                    log.debug("Processing snapshot for hub {}", snapshot.getHubId());
                    analyzer.processSnapshot(snapshot);
                } catch (Exception e) {
                    log.error("Error processing snapshot", e);
                }
            });
            consumer.commitSync();
        }
    }

    private <T> T deserializeAvro(byte[] data, Class<T> clazz) throws Exception {
        var reader = new SpecificDatumReader<>(clazz);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);
        return clazz.cast(reader.read(null, decoder));
    }

    @PreDestroy
    public void shutdown() {
        if (consumer != null) {
            consumer.close();
        }
    }
}
