package ru.yandex.practicum.interaction;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AssemblyProductsForOrderRequest {
    private Map<UUID, Integer> products;
    private UUID orderId;
}
