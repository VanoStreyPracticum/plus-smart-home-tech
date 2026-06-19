package ru.yandex.practicum.interaction;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryDto {
    private UUID deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;
}
