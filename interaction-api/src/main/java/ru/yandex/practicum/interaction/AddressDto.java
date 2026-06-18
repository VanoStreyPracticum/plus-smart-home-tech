package ru.yandex.practicum.interaction;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressDto {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
