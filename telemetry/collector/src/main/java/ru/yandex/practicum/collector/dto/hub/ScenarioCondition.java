package ru.yandex.practicum.collector.dto.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class ScenarioCondition {
    private String sensorId;
    private String type;
    private String operation;
    private Integer value;
}