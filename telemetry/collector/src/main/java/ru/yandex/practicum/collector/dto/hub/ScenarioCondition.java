package ru.yandex.practicum.collector.dto.hub;

public class ScenarioCondition {
    private String sensorId;
    private String type;
    private String operation;
    private Integer value;

    public ScenarioCondition() {}

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
