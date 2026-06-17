package ru.yandex.practicum.interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartFeignClient {
    @GetMapping("/cart")
    ShoppingCartDto getCart(@RequestParam String userName);
    
    @PostMapping("/cart")
    ShoppingCartDto addToCart(@RequestParam String userName, @RequestBody ChangeCartRequestDto request);
    
    @PutMapping("/cart")
    ShoppingCartDto updateCart(@RequestParam String userName, @RequestBody ChangeCartRequestDto request);
    
    @DeleteMapping("/cart")
    ShoppingCartDto deactivateCart(@RequestParam String userName);
}
