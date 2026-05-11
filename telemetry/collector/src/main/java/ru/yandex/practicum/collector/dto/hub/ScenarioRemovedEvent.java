package ru.yandex.practicum.collector.dto.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter @Setter @ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {

    @NotNull @Size(min = 3)
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}