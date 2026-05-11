package ru.yandex.practicum.collector.dto.hub;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ScenarioRemovedEvent extends HubEvent {

    @NotNull @Size(min = 3)
    private String name;

    public ScenarioRemovedEvent() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public HubEventType getType() { return HubEventType.SCENARIO_REMOVED; }
}
