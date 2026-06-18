package ru.yandex.practicum.warehouse.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.warehouse.model.OrderBooking;
import ru.yandex.practicum.warehouse.model.WarehouseEntity;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final OrderBookingRepository orderBookingRepository;
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private String currentAddress;

    @PostConstruct
    public void init() {
        currentAddress = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];
    }

    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (warehouseRepository.findByProductId(request.getProductId()).isPresent()) {
            throw new RuntimeException("Product already exists in warehouse");
        }
        WarehouseEntity entity = WarehouseEntity.builder()
                .productId(request.getProductId())
                .fragile(request.getFragile() != null && request.getFragile())
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .weight(request.getWeight())
                .quantity(0)
                .build();
        warehouseRepository.save(entity);
    }

    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseEntity entity = warehouseRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found in warehouse"));
        entity.setQuantity(entity.getQuantity() + request.getQuantity());
        warehouseRepository.save(entity);
    }

    public BookedProductsDto checkCart(ShoppingCartDto cartDto) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;
        for (var entry : cartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();
            WarehouseEntity entity = warehouseRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (entity.getQuantity() < quantity) {
                throw new RuntimeException("Not enough stock for product: " + productId);
            }
            totalWeight += (entity.getWeight() != null ? entity.getWeight() : 0) * quantity;
            double vol = (entity.getWidth() != null ? entity.getWidth() : 0) *
                         (entity.getHeight() != null ? entity.getHeight() : 0) *
                         (entity.getDepth() != null ? entity.getDepth() : 0);
            totalVolume += vol * quantity;
            if (entity.isFragile()) fragile = true;
        }
        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;
        for (var entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();
            WarehouseEntity entity = warehouseRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (entity.getQuantity() < quantity) {
                throw new RuntimeException("Not enough stock for product: " + productId);
            }
            entity.setQuantity(entity.getQuantity() - quantity);
            warehouseRepository.save(entity);
            double weight = entity.getWeight() != null ? entity.getWeight() : 0;
            double vol = (entity.getWidth() != null ? entity.getWidth() : 0) *
                         (entity.getHeight() != null ? entity.getHeight() : 0) *
                         (entity.getDepth() != null ? entity.getDepth() : 0);
            totalWeight += weight * quantity;
            totalVolume += vol * quantity;
            if (entity.isFragile()) fragile = true;
            OrderBooking booking = OrderBooking.builder()
                    .orderId(request.getOrderId())
                    .productId(productId)
                    .quantity(quantity)
                    .weight(weight)
                    .volume(vol)
                    .fragile(entity.isFragile())
                    .build();
            orderBookingRepository.save(booking);
        }
        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        List<OrderBooking> bookings = orderBookingRepository.findByOrderId(request.getOrderId());
        for (OrderBooking booking : bookings) {
            booking.setDeliveryId(request.getDeliveryId());
            orderBookingRepository.save(booking);
        }
    }

    @Transactional
    public void acceptReturn(Map<UUID, Integer> products) {
        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            int quantity = entry.getValue();
            WarehouseEntity entity = warehouseRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            entity.setQuantity(entity.getQuantity() + quantity);
            warehouseRepository.save(entity);
        }
    }

    public AddressDto getAddress() {
        return AddressDto.builder()
                .country(currentAddress)
                .city(currentAddress)
                .street(currentAddress)
                .house(currentAddress)
                .flat(currentAddress)
                .build();
    }
}
