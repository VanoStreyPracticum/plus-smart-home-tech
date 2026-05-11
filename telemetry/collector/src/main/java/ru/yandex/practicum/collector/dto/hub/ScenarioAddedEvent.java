package ru.yandex.practicum.collector.dto.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter @Setter @ToString(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {

    @NotNull @Size(min = 3)
    private String name;

    @NotEmpty
    private List<ScenarioCondition> conditions;

    @NotEmpty
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}