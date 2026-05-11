package ru.yandex.practicum.collector.service;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, SpecificRecordBase event) {
        try {
            byte[] bytes = serializeAvro(event);
            kafkaTemplate.send(topic, key, bytes)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Sent event to topic {}: {}", topic, event);
                        } else {
                            log.error("Failed to send event to topic {}", topic, ex);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to serialize Avro event", e);
            throw new RuntimeException("Avro serialization failed", e);
        }
    }

    private byte[] serializeAvro(SpecificRecordBase record) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            SpecificDatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(record.getSchema());
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        }
    }
}
