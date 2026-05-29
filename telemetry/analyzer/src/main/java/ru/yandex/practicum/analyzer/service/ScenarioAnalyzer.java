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
            return;
        }

        // Извлекаем значения из всех состояний датчиков в снапшоте
        Map<String, Object> sensorValues = new HashMap<>();
        snapshot.getSensorsState().forEach((sensorId, state) -> {
            Object data = state.getData();
            if (data instanceof ClimateSensorAvro climate) {
                sensorValues.put("temperatureC", climate.getTemperatureC());
                sensorValues.put("humidity", climate.getHumidity());
                sensorValues.put("co2Level", climate.getCo2Level());
            } else if (data instanceof LightSensorAvro light) {
                sensorValues.put("luminosity", light.getLuminosity());
                sensorValues.put("linkQuality", light.getLinkQuality());
            } else if (data instanceof MotionSensorAvro motion) {
                sensorValues.put("motion", motion.getMotion());
                sensorValues.put("voltage", motion.getVoltage());
            } else if (data instanceof SwitchSensorAvro sw) {
                sensorValues.put("state", sw.getState());
            } else if (data instanceof TemperatureSensorAvro temp) {
                sensorValues.put("temperatureC", temp.getTemperatureC());
                sensorValues.put("temperatureF", temp.getTemperatureF());
            }
        });

        for (Scenario scenario : scenarios) {
            boolean conditionsMet = scenario.getConditions().stream()
                    .allMatch(sc -> checkCondition(sc, sensorValues));
            if (conditionsMet) {
                log.info("Executing scenario '{}' for hub {}", scenario.getName(), hubId);
                executeActions(scenario, sensorValues);
            }
        }
    }

    private boolean checkCondition(ScenarioCondition sc, Map<String, Object> sensorValues) {
        Condition condition = sc.getCondition();
        String sensorId = sc.getSensor().getId();

        Object actualObj = sensorValues.get(condition.getType().toLowerCase());
        if (actualObj == null) return false;

        int actualValue;
        if (actualObj instanceof Boolean) {
            actualValue = (Boolean) actualObj ? 1 : 0;
        } else {
            actualValue = ((Number) actualObj).intValue();
        }

        int reference = condition.getValue();
        return switch (condition.getOperation().toUpperCase()) {
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
                    .setType(ru.yandex.practicum.grpc.telemetry.collector.ActionTypeProto.valueOf(action.getType()))
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
