package ru.yandex.practicum.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID orderId;

    private UUID shoppingCartId;
    private String userName;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;

    private UUID paymentId;
    private UUID deliveryId;

    private String state;

    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private Double totalPrice;
    private Double deliveryPrice;
    private Double productPrice;
}
