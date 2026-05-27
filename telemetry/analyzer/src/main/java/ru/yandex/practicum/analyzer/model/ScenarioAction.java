package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scenario_actions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @ManyToOne
    @MapsId("scenarioId")
    private Scenario scenario;

    @ManyToOne
    @MapsId("sensorId")
    private Sensor sensor;

    @ManyToOne
    @MapsId("actionId")
    private Action action;
}
