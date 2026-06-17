package ru.yandex.practicum.warehouse.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.AddressDto;
import ru.yandex.practicum.interaction.ShoppingCartDto;
import ru.yandex.practicum.warehouse.service.WarehouseService;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping("/warehouse/check")
    public Map<Long, Boolean> checkCart(@RequestBody ShoppingCartDto cartDto) {
        return warehouseService.checkCart(cartDto);
    }

    @GetMapping("/warehouse/address")
    public AddressDto getAddress() {
        return warehouseService.getAddress();
    }
}
