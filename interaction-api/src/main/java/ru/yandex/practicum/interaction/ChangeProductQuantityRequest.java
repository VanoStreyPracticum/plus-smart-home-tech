package ru.yandex.practicum.interaction;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChangeProductQuantityRequest {
    private UUID productId;
    private Integer newQuantity;
}
