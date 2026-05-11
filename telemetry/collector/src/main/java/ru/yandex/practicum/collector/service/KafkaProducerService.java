package ru.yandex.practicum.collector.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, SpecificRecordBase> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, SpecificRecordBase event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent event to topic {}: {}", topic, event);
                    } else {
                        log.error("Failed to send event to topic {}", topic, ex);
                    }
                });
    }
}
