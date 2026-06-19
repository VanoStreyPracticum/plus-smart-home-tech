package ru.yandex.practicum.interaction;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookedProductsDto {
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
}
