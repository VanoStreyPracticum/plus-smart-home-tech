package ru.yandex.practicum.interaction;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDto {
    private UUID paymentId;
    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
}
