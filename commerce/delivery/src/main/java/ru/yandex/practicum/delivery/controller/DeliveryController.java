package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PutMapping
    public ResponseEntity<DeliveryDto> planDelivery(@RequestBody DeliveryDto deliveryDto) {
        return ResponseEntity.ok(deliveryService.planDelivery(deliveryDto));
    }

    @PostMapping("/cost")
    public ResponseEntity<Double> deliveryCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(deliveryService.deliveryCost(order));
    }

    @PostMapping("/successful")
    public ResponseEntity<Void> deliverySuccessful(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        deliveryService.deliverySuccessful(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/picked")
    public ResponseEntity<Void> deliveryPicked(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        deliveryService.deliveryPicked(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/failed")
    public ResponseEntity<Void> deliveryFailed(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        deliveryService.deliveryFailed(id);
        return ResponseEntity.ok().build();
    }
}
