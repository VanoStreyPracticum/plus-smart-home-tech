package ru.yandex.practicum.interaction;
import lombok.*;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChangeCartRequestDto {
    private Map<Long, Integer> products;
}
