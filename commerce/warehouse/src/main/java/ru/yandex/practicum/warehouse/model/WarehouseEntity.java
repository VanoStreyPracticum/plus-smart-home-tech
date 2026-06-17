package ru.yandex.practicum.warehouse.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long productId;
    private Integer quantity;
    private Double width;
    private Double height;
    private Double depth;
    private Double weight;
    private boolean fragile;
}
