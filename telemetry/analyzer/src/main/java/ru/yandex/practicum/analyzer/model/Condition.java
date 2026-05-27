package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conditions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String operation;
    private Integer value;
}
