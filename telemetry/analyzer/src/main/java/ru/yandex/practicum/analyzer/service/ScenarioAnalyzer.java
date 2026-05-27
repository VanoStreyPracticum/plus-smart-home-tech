package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScenarioAnalyzer {

    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterClient;

    public void processSnapshot(SensorEventAvro snapshot) {
        String hubId = snapshot.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            return;
        }

        Map<String, Object> sensorValues = extractSensorValues(snapshot);

        for (Scenario scenario : scenarios) {
            boolean conditionsMet = scenario.getConditions().stream()
                    .allMatch(sc -> checkCondition(sc, sensorValues));
            if (conditionsMet) {
                log.info("Executing scenario '{}' for hub {}", scenario.getName(), hubId);
                executeActions(scenario, sensorValues);
            }
        }
    }

    // ... остальные методы без изменений (приведу их сокращённо)
    private Map<String, Object> extractSensorValues(SensorEventAvro snapshot) {
        // ... код как раньше (без Lombok-зависимости, но мы его уже написали)
        Map<String, Object> values = new HashMap<>();
        Object payload = snapshot.getPayload();
        if (payload instanceof ClimateSensorAvro climate) {
            values.put("temperatureC", climate.getTemperatureC());
            values.put("humidity", climate.getHumidity());
            values.put("co2Level", climate.getCo2Level());
        } else if (payload instanceof LightSensorAvro light) {
            values.put("linkQuality", light.getLinkQuality());
            values.put("luminosity", light.getLuminosity());
        } else if (payload instanceof MotionSensorAvro motion) {
            values.put("motion", motion.getMotion());
            values.put("voltage", motion.getVoltage());
        } else if (payload instanceof SwitchSensorAvro sw) {
            values.put("state", sw.getState());
        } else if (payload instanceof TemperatureSensorAvro temp) {
            values.put("temperatureC", temp.getTemperatureC());
            values.put("temperatureF", temp.getTemperatureF());
        }
        values.put("sensorId", snapshot.getId());
        return values;
    }

    private boolean checkCondition(ScenarioCondition sc, Map<String, Object> sensorValues) {
        Condition condition = sc.getCondition();
        String sensorId = sc.getSensor().getId();
        if (!sensorId.equals(sensorValues.get("sensorId"))) {
            return false;
        }
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
                    .setType(action.getType())
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
