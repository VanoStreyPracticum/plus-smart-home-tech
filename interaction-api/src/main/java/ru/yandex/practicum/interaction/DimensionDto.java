package ru.yandex.practicum.interaction;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DimensionDto {
    private Double width;
    private Double height;
    private Double depth;
}
