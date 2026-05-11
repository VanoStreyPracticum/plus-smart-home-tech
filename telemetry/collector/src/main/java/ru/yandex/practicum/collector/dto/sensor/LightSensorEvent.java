package ru.yandex.practicum.collector.dto.sensor;

public class LightSensorEvent extends SensorEvent {
    private int linkQuality;
    private int luminosity;

    public LightSensorEvent() {}

    public int getLinkQuality() { return linkQuality; }
    public void setLinkQuality(int linkQuality) { this.linkQuality = linkQuality; }

    public int getLuminosity() { return luminosity; }
    public void setLuminosity(int luminosity) { this.luminosity = luminosity; }

    @Override
    public SensorEventType getType() { return SensorEventType.LIGHT_SENSOR_EVENT; }
}
