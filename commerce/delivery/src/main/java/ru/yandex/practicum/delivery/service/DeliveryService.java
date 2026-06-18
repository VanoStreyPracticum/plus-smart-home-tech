package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.delivery.model.DeliveryEntity;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final OrderFeignClient orderFeignClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        DeliveryEntity entity = toEntity(deliveryDto);
        entity.setDeliveryId(UUID.randomUUID());
        entity.setDeliveryState(DeliveryState.CREATED.name());
        entity = deliveryRepository.save(entity);
        return toDto(entity);
    }

    public Double deliveryCost(OrderDto order) {
        AddressDto warehouseAddress = warehouseFeignClient.getAddress();
        double baseCost = 5.0;
        double factor = 1.0;
        if ("ADDRESS_2".equals(warehouseAddress.getStreet())) {
            factor = 2.0;
        }
        double cost = baseCost * factor + baseCost;
        if (order.getFragile() != null && order.getFragile()) {
            cost += cost * 0.2;
        }
        if (order.getDeliveryWeight() != null) {
            cost += order.getDeliveryWeight() * 0.3;
        }
        if (order.getDeliveryVolume() != null) {
            cost += order.getDeliveryVolume() * 0.2;
        }
        if (order.getState() != null) {
            cost += cost * 0.2;
        }
        return cost;
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        DeliveryEntity entity = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        entity.setDeliveryState(DeliveryState.DELIVERED.name());
        deliveryRepository.save(entity);
        orderFeignClient.delivery("\"" + orderId + "\"");
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        DeliveryEntity entity = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        entity.setDeliveryState(DeliveryState.IN_PROGRESS.name());
        deliveryRepository.save(entity);
        ShippedToDeliveryRequest shippedRequest = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(entity.getDeliveryId())
                .build();
        warehouseFeignClient.shippedToDelivery(shippedRequest);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        DeliveryEntity entity = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        entity.setDeliveryState(DeliveryState.FAILED.name());
        deliveryRepository.save(entity);
        orderFeignClient.deliveryFailed("\"" + orderId + "\"");
    }

    private DeliveryDto toDto(DeliveryEntity entity) {
        return DeliveryDto.builder()
                .deliveryId(entity.getDeliveryId())
                .fromAddress(AddressDto.builder()
                        .country(entity.getFromCountry())
                        .city(entity.getFromCity())
                        .street(entity.getFromStreet())
                        .house(entity.getFromHouse())
                        .flat(entity.getFromFlat())
                        .build())
                .toAddress(AddressDto.builder()
                        .country(entity.getToCountry())
                        .city(entity.getToCity())
                        .street(entity.getToStreet())
                        .house(entity.getToHouse())
                        .flat(entity.getToFlat())
                        .build())
                .orderId(entity.getOrderId())
                .deliveryState(DeliveryState.valueOf(entity.getDeliveryState()))
                .build();
    }

    private DeliveryEntity toEntity(DeliveryDto dto) {
        return DeliveryEntity.builder()
                .fromCountry(dto.getFromAddress().getCountry())
                .fromCity(dto.getFromAddress().getCity())
                .fromStreet(dto.getFromAddress().getStreet())
                .fromHouse(dto.getFromAddress().getHouse())
                .fromFlat(dto.getFromAddress().getFlat())
                .toCountry(dto.getToAddress().getCountry())
                .toCity(dto.getToAddress().getCity())
                .toStreet(dto.getToAddress().getStreet())
                .toHouse(dto.getToAddress().getHouse())
                .toFlat(dto.getToAddress().getFlat())
                .orderId(dto.getOrderId())
                .build();
    }
}
