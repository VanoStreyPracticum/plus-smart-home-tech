package ru.yandex.practicum.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.cart.service.CartService;

import java.util.*;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ShoppingCartDto> getShoppingCart(@RequestParam String username) {
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @PutMapping
    public ResponseEntity<ShoppingCartDto> addProductToShoppingCart(@RequestParam String username,
                                                                    @RequestBody Map<UUID, Integer> products) {
        return ResponseEntity.ok(cartService.addProducts(username, products));
    }

    @PostMapping("/change-quantity")
    public ResponseEntity<ShoppingCartDto> changeProductQuantity(@RequestParam String username,
                                                                 @RequestBody ChangeProductQuantityRequest request) {
        return ResponseEntity.ok(cartService.changeQuantity(username, request));
    }

    @PostMapping("/remove")
    public ResponseEntity<ShoppingCartDto> removeFromShoppingCart(@RequestParam String username,
                                                                  @RequestBody List<UUID> productIds) {
        return ResponseEntity.ok(cartService.removeProducts(username, productIds));
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateCurrentShoppingCart(@RequestParam String username) {
        cartService.deactivateCart(username);
        return ResponseEntity.ok().build();
    }
}
