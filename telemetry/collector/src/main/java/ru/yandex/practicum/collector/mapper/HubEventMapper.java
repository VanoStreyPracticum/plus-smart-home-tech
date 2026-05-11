package ru.yandex.practicum.collector.mapper;

import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

public class HubEventMapper {

    public static HubEventAvro toAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        if (event instanceof DeviceAddedEvent e) {
            builder.setPayload(DeviceAddedEventAvro.newBuilder()
                    .setId(e.getId())
                    .setType(DeviceTypeAvro.valueOf(e.getDeviceType()))
                    .build());
        } else if (event instanceof DeviceRemovedEvent e) {
            builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                    .setId(e.getId())
                    .build());
        } else if (event instanceof ScenarioAddedEvent e) {
            builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                    .setName(e.getName())
                    .setConditions(e.getConditions().stream()
                            .map(c -> ScenarioConditionAvro.newBuilder()
                                    .setSensorId(c.getSensorId())
                                    .setType(ConditionTypeAvro.valueOf(c.getType()))
                                    .setOperation(ConditionOperationAvro.valueOf(c.getOperation()))
                                    .setValue(c.getValue())
                                    .build())
                            .collect(Collectors.toList()))
                    .setActions(e.getActions().stream()
                            .map(a -> DeviceActionAvro.newBuilder()
                                    .setSensorId(a.getSensorId())
                                    .setType(ActionTypeAvro.valueOf(a.getType()))
                                    .setValue(a.getValue())
                                    .build())
                            .collect(Collectors.toList()))
                    .build());
        } else if (event instanceof ScenarioRemovedEvent e) {
            builder.setPayload(ScenarioRemovedEventAvro.newBuilder()
                    .setName(e.getName())
                    .build());
        } else {
            throw new IllegalArgumentException("Unknown hub event type: " + event.getType());
        }
        return builder.build();
    }
}