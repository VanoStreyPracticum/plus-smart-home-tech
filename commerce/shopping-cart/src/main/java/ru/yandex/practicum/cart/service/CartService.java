package ru.yandex.practicum.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.cart.model.CartEntity;
import ru.yandex.practicum.cart.repository.CartRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final WarehouseFeignClient warehouseFeignClient;

    public ShoppingCartDto getCart(String userName) {
        CartEntity entity = cartRepository.findByUserName(userName)
                .orElseGet(() -> createEmptyCart(userName));
        return toDto(entity);
    }

    public ShoppingCartDto addProducts(String userName, Map<UUID, Integer> products) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseGet(() -> createEmptyCart(userName));
        ShoppingCartDto tempCart = toDto(cart);
        tempCart.getProducts().putAll(products);
        warehouseFeignClient.checkCart(tempCart); // выбросит исключение при недостатке
        cart.getProducts().putAll(products);
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    public ShoppingCartDto changeQuantity(String userName, ChangeProductQuantityRequest request) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        if (request.getNewQuantity() <= 0) {
            cart.getProducts().remove(request.getProductId());
        } else {
            cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        }
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    public ShoppingCartDto removeProducts(String userName, List<UUID> productIds) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        productIds.forEach(cart.getProducts()::remove);
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    public void deactivateCart(String userName) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.setActive(false);
        cartRepository.save(cart);
    }

    private CartEntity createEmptyCart(String userName) {
        CartEntity cart = CartEntity.builder()
                .shoppingCartId(UUID.randomUUID())
                .userName(userName)
                .products(new HashMap<>())
                .active(true)
                .build();
        return cartRepository.save(cart);
    }

    private ShoppingCartDto toDto(CartEntity entity) {
        return ShoppingCartDto.builder()
                .shoppingCartId(entity.getShoppingCartId())
                .products(entity.getProducts())
                .build();
    }
}
