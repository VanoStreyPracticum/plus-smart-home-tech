package ru.yandex.practicum.interaction;
import lombok.*;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShoppingCartDto {
    private Long id;
    private String userName;
    private Map<Long, Integer> products;
    private boolean active;
}
