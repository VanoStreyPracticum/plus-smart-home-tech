package ru.yandex.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID deliveryId;

    private String fromCountry;
    private String fromCity;
    private String fromStreet;
    private String fromHouse;
    private String fromFlat;

    private String toCountry;
    private String toCity;
    private String toStreet;
    private String toHouse;
    private String toFlat;

    private UUID orderId;
    private String deliveryState;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
}
