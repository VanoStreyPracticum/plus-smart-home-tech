package ru.yandex.practicum.collector.dto.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private String deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}