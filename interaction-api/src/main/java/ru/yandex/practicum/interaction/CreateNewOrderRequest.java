package ru.yandex.practicum.interaction;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateNewOrderRequest {
    private ShoppingCartDto shoppingCart;
    private AddressDto deliveryAddress;
}
