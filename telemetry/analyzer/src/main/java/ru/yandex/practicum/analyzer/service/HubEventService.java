package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void handleDeviceAdded(HubEventAvro event) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) event.getPayload();
        Sensor sensor = new Sensor();
        sensor.setId(payload.getId());
        sensor.setHubId(event.getHubId());
        sensorRepository.save(sensor);
        log.info("Added sensor: {}", sensor.getId());
    }

    @Transactional
    public void handleDeviceRemoved(HubEventAvro event) {
        DeviceRemovedEventAvro payload = (DeviceRemovedEventAvro) event.getPayload();
        sensorRepository.deleteById(payload.getId());
        log.info("Removed sensor: {}", payload.getId());
    }

    @Transactional
    public void handleScenarioAdded(HubEventAvro event) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) event.getPayload();
        Scenario scenario = scenarioRepository
                .findByHubIdAndName(event.getHubId(), payload.getName())
                .orElseGet(() -> {
                    Scenario s = new Scenario();
                    s.setHubId(event.getHubId());
                    s.setName(payload.getName());
                    return scenarioRepository.save(s);
                });

        // Удаляем старые условия и действия (если сценарий обновляется)
        scenario.getConditions().clear();
        scenario.getActions().clear();

        // Сохраняем условия
        for (ScenarioConditionAvro condAvro : payload.getConditions()) {
            Condition condition = new Condition();
            condition.setType(condAvro.getType().name());
            condition.setOperation(condAvro.getOperation().name());
            
            // Безопасное приведение Object -> Integer
            Object rawValue = condAvro.getValue();
            if (rawValue instanceof Integer) {
                condition.setValue((Integer) rawValue);
            } else if (rawValue instanceof Boolean) {
                condition.setValue((Boolean) rawValue ? 1 : 0);
            } else {
                condition.setValue(null); // или 0 в зависимости от требований
            }
            conditionRepository.save(condition);

            ScenarioConditionId scId = new ScenarioConditionId();
            scId.setScenarioId(scenario.getId());
            scId.setSensorId(condAvro.getSensorId());
            scId.setConditionId(condition.getId());

            ScenarioCondition sc = new ScenarioCondition();
            sc.setId(scId);
            sc.setScenario(scenario);
            sc.setSensor(sensorRepository.getReferenceById(condAvro.getSensorId()));
            sc.setCondition(condition);
            scenario.getConditions().add(sc);
        }

        // Сохраняем действия
        for (DeviceActionAvro actionAvro : payload.getActions()) {
            Action action = new Action();
            action.setType(actionAvro.getType().name());
            
            Object rawActionValue = actionAvro.getValue();
            if (rawActionValue instanceof Integer) {
                action.setValue((Integer) rawActionValue);
            } else {
                action.setValue(null);
            }
            actionRepository.save(action);

            ScenarioActionId saId = new ScenarioActionId();
            saId.setScenarioId(scenario.getId());
            saId.setSensorId(actionAvro.getSensorId());
            saId.setActionId(action.getId());

            ScenarioAction sa = new ScenarioAction();
            sa.setId(saId);
            sa.setScenario(scenario);
            sa.setSensor(sensorRepository.getReferenceById(actionAvro.getSensorId()));
            sa.setAction(action);
            scenario.getActions().add(sa);
        }

        scenarioRepository.save(scenario);
        log.info("Saved scenario: {} for hub {}", scenario.getName(), event.getHubId());
    }

    @Transactional
    public void handleScenarioRemoved(HubEventAvro event) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) event.getPayload();
        scenarioRepository.findByHubIdAndName(event.getHubId(), payload.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Removed scenario: {}", payload.getName());
                });
    }
}
