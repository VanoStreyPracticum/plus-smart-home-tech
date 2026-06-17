package ru.yandex.practicum.interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartFeignClient {
    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getCart(@RequestParam String username);
}
