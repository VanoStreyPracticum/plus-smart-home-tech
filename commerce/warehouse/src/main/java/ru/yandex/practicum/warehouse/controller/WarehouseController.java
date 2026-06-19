package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @PutMapping
    public ResponseEntity<Void> newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        warehouseService.newProductInWarehouse(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check")
    public ResponseEntity<BookedProductsDto> checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto cartDto) {
        return ResponseEntity.ok(warehouseService.checkCart(cartDto));
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/address")
    public ResponseEntity<AddressDto> getWarehouseAddress() {
        return ResponseEntity.ok(warehouseService.getAddress());
    }

    @PostMapping("/assembly")
    public ResponseEntity<BookedProductsDto> assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request) {
        return ResponseEntity.ok(warehouseService.assemblyProductsForOrder(request));
    }

    @PostMapping("/shipped")
    public ResponseEntity<Void> shippedToDelivery(@RequestBody ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/return")
    public ResponseEntity<Void> acceptReturn(@RequestBody Map<UUID, Integer> products) {
        warehouseService.acceptReturn(products);
        return ResponseEntity.ok().build();
    }
}
