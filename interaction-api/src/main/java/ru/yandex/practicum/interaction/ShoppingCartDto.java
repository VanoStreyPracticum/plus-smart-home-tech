package ru.yandex.practicum.interaction;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Integer> products;
}
