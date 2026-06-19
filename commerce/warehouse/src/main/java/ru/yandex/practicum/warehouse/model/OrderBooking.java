package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID orderId;
    private UUID deliveryId;
    private UUID productId;
    private Integer quantity;
    private Double weight;
    private Double volume;
    private Boolean fragile;
}
