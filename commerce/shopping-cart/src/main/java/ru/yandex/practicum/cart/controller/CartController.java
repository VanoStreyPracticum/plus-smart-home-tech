package ru.yandex.practicum.cart.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.ChangeCartRequestDto;
import ru.yandex.practicum.interaction.ShoppingCartDto;
import ru.yandex.practicum.cart.service.CartService;

@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart")
    public ShoppingCartDto getCart(@RequestParam String userName) {
        return cartService.getCart(userName);
    }

    @PostMapping("/cart")
    public ShoppingCartDto addToCart(@RequestParam String userName, @RequestBody ChangeCartRequestDto request) {
        return cartService.addToCart(userName, request);
    }

    @PutMapping("/cart")
    public ShoppingCartDto updateCart(@RequestParam String userName, @RequestBody ChangeCartRequestDto request) {
        return cartService.updateCart(userName, request);
    }

    @DeleteMapping("/cart")
    public ShoppingCartDto deactivateCart(@RequestParam String userName) {
        return cartService.deactivateCart(userName);
    }
}
