package ru.yandex.practicum.interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient {
    @GetMapping("/warehouse/check")
    Map<Long, Boolean> checkCart(@RequestBody ShoppingCartDto cartDto);
    
    @GetMapping("/warehouse/address")
    AddressDto getAddress();
}
