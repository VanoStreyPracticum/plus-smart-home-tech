package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.grpc.telemetry.collector.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.collector.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScenarioAnalyzer {

    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterClient;

    @Transactional(readOnly = true)
    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("No scenarios for hub {}", hubId);
            return;
        }

        // Извлекаем значения датчиков, ключи соответствуют типам условий
        Map<String, Object> sensorValues = new HashMap<>();
        snapshot.getSensorsState().forEach((sensorId, state) -> {
            Object data = state.getData();
            if (data instanceof ClimateSensorAvro climate) {
                sensorValues.put("TEMPERATURE", climate.getTemperatureC());
                sensorValues.put("HUMIDITY", climate.getHumidity());
                sensorValues.put("CO2LEVEL", climate.getCo2Level());
            } else if (data instanceof LightSensorAvro light) {
                sensorValues.put("LUMINOSITY", light.getLuminosity());
                sensorValues.put("LINK_QUALITY", light.getLinkQuality());
            } else if (data instanceof MotionSensorAvro motion) {
                sensorValues.put("MOTION", motion.getMotion());
                sensorValues.put("VOLTAGE", motion.getVoltage());
            } else if (data instanceof SwitchSensorAvro sw) {
                sensorValues.put("SWITCH", sw.getState());
            } else if (data instanceof TemperatureSensorAvro temp) {
                sensorValues.put("TEMPERATURE", temp.getTemperatureC());
            }
        });

        for (Scenario scenario : scenarios) {
            boolean conditionsMet = scenario.getConditions().stream()
                    .allMatch(sc -> checkCondition(sc, sensorValues));
            if (conditionsMet) {
                log.info("Executing scenario '{}' for hub {}", scenario.getName(), hubId);
                executeActions(scenario, sensorValues);
            } else {
                log.debug("Scenario '{}' conditions not met", scenario.getName());
            }
        }
    }

    private boolean checkCondition(ScenarioCondition sc, Map<String, Object> sensorValues) {
        Condition condition = sc.getCondition();
        String type = condition.getType().toUpperCase(); // TEMPERATURE, MOTION, SWITCH, ...
        Object actualObj = sensorValues.get(type);
        if (actualObj == null) {
            log.warn("No sensor value for condition type {} (sensor {})", type, sc.getSensor().getId());
            return false;
        }

        int actualValue;
        if (actualObj instanceof Boolean) {
            actualValue = (Boolean) actualObj ? 1 : 0;
        } else {
            actualValue = ((Number) actualObj).intValue();
        }

        int reference = condition.getValue();
        String operation = condition.getOperation().toUpperCase();
        log.debug("Checking condition: {} {} {} (actual: {})", type, operation, reference, actualValue);
        return switch (operation) {
            case "EQUALS" -> actualValue == reference;
            case "GREATER_THAN" -> actualValue > reference;
            case "LOWER_THAN" -> actualValue < reference;
            default -> false;
        };
    }

    private void executeActions(Scenario scenario, Map<String, Object> sensorValues) {
        for (ScenarioAction sa : scenario.getActions()) {
            Action action = sa.getAction();
            DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                    .setSensorId(sa.getSensor().getId())
                    .setType(ActionTypeProto.valueOf(action.getType()))
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(scenario.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(deviceAction)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            try {
                Empty response = hubRouterClient.handleDeviceAction(request);
                log.debug("Sent action to hub-router: {}", request);
            } catch (Exception e) {
                log.error("Failed to send action for scenario {}", scenario.getName(), e);
            }
        }
    }
}
