package ru.yandex.practicum.payment.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID paymentId;

    private UUID orderId;
    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
    private String state; // PENDING, SUCCESS, FAILED
}
