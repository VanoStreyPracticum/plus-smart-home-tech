package ru.yandex.practicum.collector.dto.hub;

public class DeviceAction {
    private String sensorId;
    private String type;
    private Integer value;

    public DeviceAction() {}

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
