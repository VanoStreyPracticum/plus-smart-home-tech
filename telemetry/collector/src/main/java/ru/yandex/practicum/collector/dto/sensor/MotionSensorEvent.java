package ru.yandex.practicum.collector.dto.sensor;

public class MotionSensorEvent extends SensorEvent {
    private int linkQuality;
    private boolean motion;
    private int voltage;

    public MotionSensorEvent() {}

    public int getLinkQuality() { return linkQuality; }
    public void setLinkQuality(int linkQuality) { this.linkQuality = linkQuality; }

    public boolean isMotion() { return motion; }
    public void setMotion(boolean motion) { this.motion = motion; }

    public int getVoltage() { return voltage; }
    public void setVoltage(int voltage) { this.voltage = voltage; }

    @Override
    public SensorEventType getType() { return SensorEventType.MOTION_SENSOR_EVENT; }
}
