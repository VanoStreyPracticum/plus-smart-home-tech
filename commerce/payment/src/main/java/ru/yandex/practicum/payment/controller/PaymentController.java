package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/productCost")
    public ResponseEntity<Double> productCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(paymentService.productCost(order));
    }

    @PostMapping("/totalCost")
    public ResponseEntity<Double> getTotalCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(paymentService.getTotalCost(order));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> payment(@RequestBody OrderDto order) {
        return ResponseEntity.ok(paymentService.payment(order));
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> paymentSuccess(@RequestBody String paymentId) {
        UUID id = UUID.fromString(paymentId.replaceAll("^\"|\"$", "").trim());
        paymentService.paymentSuccess(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/failed")
    public ResponseEntity<Void> paymentFailed(@RequestBody String paymentId) {
        UUID id = UUID.fromString(paymentId.replaceAll("^\"|\"$", "").trim());
        paymentService.paymentFailed(id);
        return ResponseEntity.ok().build();
    }
}
