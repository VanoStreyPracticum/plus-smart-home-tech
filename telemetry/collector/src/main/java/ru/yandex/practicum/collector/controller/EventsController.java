package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.dto.hub.HubEvent;
import ru.yandex.practicum.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.service.KafkaProducerService;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventsController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/events/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Received sensor event: {}", event);
        kafkaProducerService.send("telemetry.sensors.v1", event.getHubId(), SensorEventMapper.toAvro(event));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Received hub event: {}", event);
        kafkaProducerService.send("telemetry.hubs.v1", event.getHubId(), HubEventMapper.toAvro(event));
        return ResponseEntity.ok().build();
    }
}