package ru.yandex.practicum.warehouse.service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interaction.AddressDto;
import ru.yandex.practicum.interaction.ShoppingCartDto;
import ru.yandex.practicum.warehouse.model.WarehouseEntity;
import ru.yandex.practicum.warehouse.repository.WarehouseRepository;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private String currentAddress;

    @PostConstruct
    public void init() {
        currentAddress = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];
    }

    public Map<Long, Boolean> checkCart(ShoppingCartDto cartDto) {
        Map<Long, Boolean> result = new HashMap<>();
        cartDto.getProducts().forEach((productId, quantity) -> {
            WarehouseEntity entity = warehouseRepository.findByProductId(productId)
                    .orElse(WarehouseEntity.builder().productId(productId).quantity(0).build());
            result.put(productId, entity.getQuantity() >= quantity);
        });
        return result;
    }

    public AddressDto getAddress() {
        return AddressDto.builder()
                .country(currentAddress)
                .city(currentAddress)
                .street(currentAddress)
                .house(currentAddress)
                .apartment(currentAddress)
                .build();
    }
}
