package ru.yandex.practicum.warehouse.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.warehouse.model.WarehouseEntity;
import ru.yandex.practicum.warehouse.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
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

    public Map<UUID, Boolean> checkCart(ShoppingCartDto cartDto) {
        Map<UUID, Boolean> result = new HashMap<>();
        cartDto.getProducts().forEach((productId, quantity) -> {
            WarehouseEntity entity = warehouseRepository.findByProductId(productId)
                    .orElse(WarehouseEntity.builder().productId(productId).quantity(0).build());
            result.put(productId, entity.getQuantity() >= quantity);
        });
        return result;
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
