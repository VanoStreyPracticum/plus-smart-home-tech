package ru.yandex.practicum.cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID shoppingCartId;

    @Column(unique = true)
    private String userName;

    @ElementCollection
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;

    private boolean active;
}
