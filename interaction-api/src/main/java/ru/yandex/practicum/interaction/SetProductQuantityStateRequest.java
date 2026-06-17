package ru.yandex.practicum.interaction;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SetProductQuantityStateRequest {
    private UUID productId;
    private QuantityState quantityState;
}
