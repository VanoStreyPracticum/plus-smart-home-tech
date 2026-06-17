package ru.yandex.practicum.cart.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.cart.model.CartEntity;
import ru.yandex.practicum.cart.repository.CartRepository;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final WarehouseFeignClient warehouseFeignClient;

    public ShoppingCartDto getCart(String userName) {
        CartEntity entity = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return toDto(entity);
    }

    public ShoppingCartDto addToCart(String userName, ChangeCartRequestDto request) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElse(CartEntity.builder().userName(userName).products(Map.of()).active(true).build());
        // проверка на складе
        ShoppingCartDto tempCart = toDto(cart);
        tempCart.getProducts().putAll(request.getProducts());
        Map<Long, Boolean> availability = warehouseFeignClient.checkCart(tempCart);
        if (availability.containsValue(false)) {
            throw new RuntimeException("Not enough stock for: " +
                availability.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey().toString()).collect(Collectors.joining(",")));
        }
        cart.getProducts().putAll(request.getProducts());
        cart.setActive(true);
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    public ShoppingCartDto updateCart(String userName, ChangeCartRequestDto request) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.setProducts(request.getProducts());
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    public ShoppingCartDto deactivateCart(String userName) {
        CartEntity cart = cartRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.setActive(false);
        cart = cartRepository.save(cart);
        return toDto(cart);
    }

    private ShoppingCartDto toDto(CartEntity entity) {
        return ShoppingCartDto.builder()
                .id(entity.getId())
                .userName(entity.getUserName())
                .products(entity.getProducts())
                .active(entity.isActive())
                .build();
    }
}
