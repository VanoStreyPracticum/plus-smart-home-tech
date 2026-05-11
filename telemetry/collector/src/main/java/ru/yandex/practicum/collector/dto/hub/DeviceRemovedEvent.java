package ru.yandex.practicum.collector.dto.hub;

public class DeviceRemovedEvent extends HubEvent {
    private String id;

    public DeviceRemovedEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public HubEventType getType() { return HubEventType.DEVICE_REMOVED; }
}
