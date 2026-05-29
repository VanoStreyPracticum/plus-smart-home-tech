package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.dto.hub.*;
import ru.yandex.practicum.collector.dto.sensor.*;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.service.KafkaProducerService;
import ru.yandex.practicum.grpc.telemetry.collector.*;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final KafkaProducerService kafkaProducerService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        SensorEvent event = mapProtoToSensorEvent(request);
        log.info("Received gRPC sensor event: {}", event);
        kafkaProducerService.send("telemetry.sensors.v1", event.getHubId(), SensorEventMapper.toAvro(event));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        HubEvent event = mapProtoToHubEvent(request);
        log.info("Received gRPC hub event: {}", event);
        kafkaProducerService.send("telemetry.hubs.v1", event.getHubId(), HubEventMapper.toAvro(event));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private SensorEvent mapProtoToSensorEvent(SensorEventProto proto) {
        if (proto.hasMotionSensor()) {
            MotionSensorProto p = proto.getMotionSensor();
            MotionSensorEvent e = new MotionSensorEvent();
            fillCommonSensorFields(e, proto);
            e.setLinkQuality(p.getLinkQuality());
            e.setMotion(p.getMotion());
            e.setVoltage(p.getVoltage());
            return e;
        } else if (proto.hasTemperatureSensor()) {
            TemperatureSensorProto p = proto.getTemperatureSensor();
            TemperatureSensorEvent e = new TemperatureSensorEvent();
            fillCommonSensorFields(e, proto);
            e.setTemperatureC(p.getTemperatureC());
            e.setTemperatureF(p.getTemperatureF());
            return e;
        } else if (proto.hasLightSensor()) {
            LightSensorProto p = proto.getLightSensor();
            LightSensorEvent e = new LightSensorEvent();
            fillCommonSensorFields(e, proto);
            e.setLinkQuality(p.getLinkQuality());
            e.setLuminosity(p.getLuminosity());
            return e;
        } else if (proto.hasClimateSensor()) {
            ClimateSensorProto p = proto.getClimateSensor();
            ClimateSensorEvent e = new ClimateSensorEvent();
            fillCommonSensorFields(e, proto);
            e.setTemperatureC(p.getTemperatureC());
            e.setHumidity(p.getHumidity());
            e.setCo2Level(p.getCo2Level());
            return e;
        } else if (proto.hasSwitchSensor()) {
            SwitchSensorProto p = proto.getSwitchSensor();
            SwitchSensorEvent e = new SwitchSensorEvent();
            fillCommonSensorFields(e, proto);
            e.setState(p.getState());
            return e;
        }
        throw new IllegalArgumentException("Unknown sensor event type");
    }

    private void fillCommonSensorFields(SensorEvent event, SensorEventProto proto) {
        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()));
    }

    private HubEvent mapProtoToHubEvent(HubEventProto proto) {
        if (proto.hasDeviceAdded()) {
            DeviceAddedEventProto p = proto.getDeviceAdded();
            DeviceAddedEvent e = new DeviceAddedEvent();
            fillCommonHubFields(e, proto);
            e.setId(p.getId());
            e.setDeviceType(p.getType().name());
            return e;
        } else if (proto.hasDeviceRemoved()) {
            DeviceRemovedEventProto p = proto.getDeviceRemoved();
            DeviceRemovedEvent e = new DeviceRemovedEvent();
            fillCommonHubFields(e, proto);
            e.setId(p.getId());
            return e;
        } else if (proto.hasScenarioAdded()) {
            ScenarioAddedEventProto p = proto.getScenarioAdded();
            ScenarioAddedEvent e = new ScenarioAddedEvent();
            fillCommonHubFields(e, proto);
            e.setName(p.getName());
            e.setConditions(p.getConditionList().stream().map(c -> {
                ScenarioCondition cond = new ScenarioCondition();
                cond.setSensorId(c.getSensorId());
                cond.setType(c.getType().name());
                cond.setOperation(c.getOperation().name());
                if (c.hasIntValue()) cond.setValue(c.getIntValue());
                else if (c.hasBoolValue()) cond.setValue(c.getBoolValue() ? 1 : 0);
                return cond;
            }).toList());
            e.setActions(p.getActionList().stream().map(a -> {
                DeviceAction action = new DeviceAction();
                action.setSensorId(a.getSensorId());
                action.setType(a.getType().name());
                if (a.hasValue()) action.setValue(a.getValue());
                return action;
            }).toList());
            return e;
        } else if (proto.hasScenarioRemoved()) {
            ScenarioRemovedEventProto p = proto.getScenarioRemoved();
            ScenarioRemovedEvent e = new ScenarioRemovedEvent();
            fillCommonHubFields(e, proto);
            e.setName(p.getName());
            return e;
        }
        throw new IllegalArgumentException("Unknown hub event type");
    }

    private void fillCommonHubFields(HubEvent event, HubEventProto proto) {
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()));
    }
}
