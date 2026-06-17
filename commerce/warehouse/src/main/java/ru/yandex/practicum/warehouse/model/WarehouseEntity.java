package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "warehouse_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private UUID productId;

    private Integer quantity;
    private Double width;
    private Double height;
    private Double depth;
    private Double weight;
    private boolean fragile;
}
