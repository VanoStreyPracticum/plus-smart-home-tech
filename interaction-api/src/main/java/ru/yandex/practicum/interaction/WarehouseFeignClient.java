package ru.yandex.practicum.interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient {
    @PostMapping("/api/v1/warehouse/check")
    Map<UUID, Boolean> checkCart(@RequestBody ShoppingCartDto cartDto);
}
