package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios")
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "hub_id")
    private String hubId;
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScenarioAction> actions = new ArrayList<>();

    public Scenario() {}
    public Scenario(Long id, String hubId, String name) { this.id = id; this.hubId = hubId; this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHubId() { return hubId; }
    public void setHubId(String hubId) { this.hubId = hubId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<ScenarioCondition> getConditions() { return conditions; }
    public void setConditions(List<ScenarioCondition> conditions) { this.conditions = conditions; }
    public List<ScenarioAction> getActions() { return actions; }
    public void setActions(List<ScenarioAction> actions) { this.actions = actions; }
}
