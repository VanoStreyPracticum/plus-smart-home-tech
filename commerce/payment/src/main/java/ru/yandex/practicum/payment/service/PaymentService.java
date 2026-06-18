package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.payment.model.PaymentEntity;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ShoppingStoreFeignClient storeFeignClient;
    private final OrderFeignClient orderFeignClient;

    public Double productCost(OrderDto order) {
        double sum = 0.0;
        for (var entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();
            ProductDto product = storeFeignClient.getProduct(productId);
            sum += product.getPrice() * quantity;
        }
        return sum;
    }

    public Double getTotalCost(OrderDto order) {
        double productCost = productCost(order);
        double deliveryCost = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0;
        double vat = productCost * 0.1;
        return productCost + vat + deliveryCost;
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        double productCostVal = productCost(order);
        double totalCostVal = getTotalCost(order);
        double deliveryCostVal = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0;
        double vat = productCostVal * 0.1;

        PaymentEntity entity = PaymentEntity.builder()
                .paymentId(UUID.randomUUID())
                .orderId(order.getOrderId())
                .totalPayment(totalCostVal)
                .deliveryTotal(deliveryCostVal)
                .feeTotal(vat)
                .state("PENDING")
                .build();
        entity = paymentRepository.save(entity);
        return PaymentDto.builder()
                .paymentId(entity.getPaymentId())
                .totalPayment(entity.getTotalPayment())
                .deliveryTotal(entity.getDeliveryTotal())
                .feeTotal(entity.getFeeTotal())
                .build();
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        entity.setState("SUCCESS");
        paymentRepository.save(entity);
        // Передаём UUID как JSON-строку в кавычках
        orderFeignClient.payment("\"" + entity.getOrderId() + "\"");
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        entity.setState("FAILED");
        paymentRepository.save(entity);
        orderFeignClient.paymentFailed("\"" + entity.getOrderId() + "\"");
    }
}
