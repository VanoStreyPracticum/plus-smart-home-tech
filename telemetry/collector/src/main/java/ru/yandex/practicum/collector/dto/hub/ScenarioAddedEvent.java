package ru.yandex.practicum.collector.dto.hub;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ScenarioAddedEvent extends HubEvent {

    @NotNull @Size(min = 3)
    private String name;

    @NotEmpty
    private List<ScenarioCondition> conditions;

    @NotEmpty
    private List<DeviceAction> actions;

    public ScenarioAddedEvent() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ScenarioCondition> getConditions() { return conditions; }
    public void setConditions(List<ScenarioCondition> conditions) { this.conditions = conditions; }

    public List<DeviceAction> getActions() { return actions; }
    public void setActions(List<DeviceAction> actions) { this.actions = actions; }

    @Override
    public HubEventType getType() { return HubEventType.SCENARIO_ADDED; }
}
