package ru.yandex.practicum.interaction;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippedToDeliveryRequest {
    private UUID orderId;
    private UUID deliveryId;
}
