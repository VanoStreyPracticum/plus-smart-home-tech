package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.order.model.OrderEntity;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final DeliveryFeignClient deliveryFeignClient;
    private final PaymentFeignClient paymentFeignClient;

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request, String userName) {
        ShoppingCartDto cart = request.getShoppingCart();
        BookedProductsDto checkResult = warehouseFeignClient.checkCart(cart);
        if (checkResult == null) {
            throw new RuntimeException("Warehouse check failed");
        }
        OrderEntity order = OrderEntity.builder()
                .orderId(UUID.randomUUID())
                .shoppingCartId(cart.getShoppingCartId())
                .userName(userName)
                .products(cart.getProducts())
                .state("NEW")
                .deliveryWeight(checkResult.getDeliveryWeight())
                .deliveryVolume(checkResult.getDeliveryVolume())
                .fragile(checkResult.getFragile())
                .build();
        order = orderRepository.save(order);
        return toDto(order);
    }

    public List<OrderDto> getClientOrders(String userName) {
        return orderRepository.findByUserName(userName).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                .orderId(orderId)
                .products(order.getProducts())
                .build();
        BookedProductsDto result = warehouseFeignClient.assemblyProductsForOrder(request);
        order.setDeliveryWeight(result.getDeliveryWeight());
        order.setDeliveryVolume(result.getDeliveryVolume());
        order.setFragile(result.getFragile());
        order.setState("ASSEMBLED");
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("ASSEMBLY_FAILED");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Double cost = deliveryFeignClient.deliveryCost(toDto(order));
        order.setDeliveryPrice(cost);
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Double total = paymentFeignClient.getTotalCost(toDto(order));
        order.setTotalPrice(total);
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        PaymentDto paymentDto = paymentFeignClient.payment(toDto(order));
        order.setPaymentId(paymentDto.getPaymentId());
        order.setState("ON_PAYMENT");
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    public OrderDto paymentSuccess(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("PAID");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("PAYMENT_FAILED");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("DELIVERED");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("DELIVERY_FAILED");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setState("COMPLETED");
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        warehouseFeignClient.acceptReturn(request.getProducts());
        order.setState("PRODUCT_RETURNED");
        return toDto(orderRepository.save(order));
    }

    private OrderDto toDto(OrderEntity entity) {
        return OrderDto.builder()
                .orderId(entity.getOrderId())
                .shoppingCartId(entity.getShoppingCartId())
                .products(entity.getProducts())
                .paymentId(entity.getPaymentId())
                .deliveryId(entity.getDeliveryId())
                .state(entity.getState())
                .deliveryWeight(entity.getDeliveryWeight())
                .deliveryVolume(entity.getDeliveryVolume())
                .fragile(entity.getFragile())
                .totalPrice(entity.getTotalPrice())
                .deliveryPrice(entity.getDeliveryPrice())
                .productPrice(entity.getProductPrice())
                .build();
    }
}
