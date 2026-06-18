package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PutMapping
    public ResponseEntity<OrderDto> createNewOrder(@RequestBody CreateNewOrderRequest request,
                                                   @RequestParam String username) {
        return ResponseEntity.ok(orderService.createNewOrder(request, username));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getClientOrders(@RequestParam String username) {
        return ResponseEntity.ok(orderService.getClientOrders(username));
    }

    @PostMapping("/assembly")
    public ResponseEntity<OrderDto> assembly(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.assembly(id));
    }

    @PostMapping("/assembly/failed")
    public ResponseEntity<OrderDto> assemblyFailed(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.assemblyFailed(id));
    }

    @PostMapping("/calculate/delivery")
    public ResponseEntity<OrderDto> calculateDeliveryCost(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.calculateDeliveryCost(id));
    }

    @PostMapping("/calculate/total")
    public ResponseEntity<OrderDto> calculateTotalCost(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.calculateTotalCost(id));
    }

    @PostMapping("/payment")
    public ResponseEntity<OrderDto> payment(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.payment(id));
    }

    @PostMapping("/payment/failed")
    public ResponseEntity<OrderDto> paymentFailed(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.paymentFailed(id));
    }

    @PostMapping("/delivery")
    public ResponseEntity<OrderDto> delivery(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.delivery(id));
    }

    @PostMapping("/delivery/failed")
    public ResponseEntity<OrderDto> deliveryFailed(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.deliveryFailed(id));
    }

    @PostMapping("/completed")
    public ResponseEntity<OrderDto> complete(@RequestBody String orderId) {
        UUID id = UUID.fromString(orderId.replaceAll("^\"|\"$", "").trim());
        return ResponseEntity.ok(orderService.complete(id));
    }

    @PostMapping("/return")
    public ResponseEntity<OrderDto> productReturn(@RequestBody ProductReturnRequest request) {
        return ResponseEntity.ok(orderService.productReturn(request));
    }
}
