package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "scenario_conditions")
@Data @NoArgsConstructor @AllArgsConstructor
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

    // Явные геттеры для Lombok-совместимости
    public Condition getCondition() { return condition; }
    public Sensor getSensor() { return sensor; }
    public Scenario getScenario() { return scenario; }
}
