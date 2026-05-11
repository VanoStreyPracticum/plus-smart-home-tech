package ru.yandex.practicum.collector.mapper;

import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent event) {
        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .build();

        if (event instanceof ClimateSensorEvent e) {
            avro.setPayload(ClimateSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setHumidity(e.getHumidity())
                    .setCo2Level(e.getCo2Level())
                    .build());
        } else if (event instanceof LightSensorEvent e) {
            avro.setPayload(LightSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setLuminosity(e.getLuminosity())
                    .build());
        } else if (event instanceof MotionSensorEvent e) {
            avro.setPayload(MotionSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setMotion(e.isMotion())
                    .setVoltage(e.getVoltage())
                    .build());
        } else if (event instanceof SwitchSensorEvent e) {
            avro.setPayload(SwitchSensorAvro.newBuilder()
                    .setState(e.isState())
                    .build());
        } else if (event instanceof TemperatureSensorEvent e) {
            avro.setPayload(TemperatureSensorAvro.newBuilder()
                    .setId(e.getId())
                    .setHubId(e.getHubId())
                    .setTimestamp(e.getTimestamp())
                    .setTemperatureC(e.getTemperatureC())
                    .setTemperatureF(e.getTemperatureF())
                    .build());
        } else {
            throw new IllegalArgumentException("Unknown sensor event type: " + event.getType());
        }
        return avro;
    }
}