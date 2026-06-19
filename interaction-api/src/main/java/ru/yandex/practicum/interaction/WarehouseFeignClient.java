package ru.yandex.practicum.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient {
    @PutMapping("/api/v1/warehouse")
    void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkCart(@RequestBody ShoppingCartDto cartDto);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddress();

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/return")
    void acceptReturn(@RequestBody Map<UUID, Integer> products);
}
