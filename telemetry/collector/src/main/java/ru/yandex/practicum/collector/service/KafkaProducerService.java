package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

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