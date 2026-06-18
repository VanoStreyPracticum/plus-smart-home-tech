package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "scenario_actions")
@Data @NoArgsConstructor @AllArgsConstructor
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

    // Явные геттеры для Lombok-совместимости
    public Action getAction() { return action; }
    public Sensor getSensor() { return sensor; }
    public Scenario getScenario() { return scenario; }
}
