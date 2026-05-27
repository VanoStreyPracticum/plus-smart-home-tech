package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scenario_conditions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScenarioCondition {
    @EmbeddedId
    private ScenarioConditionId id;

    @ManyToOne
    @MapsId("scenarioId")
    private Scenario scenario;

    @ManyToOne
    @MapsId("sensorId")
    private Sensor sensor;

    @ManyToOne
    @MapsId("conditionId")
    private Condition condition;
}
