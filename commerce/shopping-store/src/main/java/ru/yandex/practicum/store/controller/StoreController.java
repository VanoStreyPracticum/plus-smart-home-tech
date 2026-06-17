package ru.yandex.practicum.store.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.store.model.ProductEntity;
import ru.yandex.practicum.store.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StoreController implements ShoppingStoreFeignClient {
    private final ProductRepository productRepository;

    @Override
    public List<ProductDto> getProducts(ProductCategory category) {
        List<ProductEntity> entities = category != null ?
                productRepository.findByCategory(category) : productRepository.findAll();
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductDto getProduct(Long id) {
        return toDto(productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found")));
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) {
        ProductEntity entity = toEntity(productDto);
        entity.setState(ProductState.ACTIVE);
        return toDto(productRepository.save(entity));
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        ProductEntity entity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setName(productDto.getName());
        entity.setDescription(productDto.getDescription());
        entity.setPrice(productDto.getPrice());
        entity.setCategory(productDto.getCategory());
        entity.setQuantityState(productDto.getQuantityState());
        return toDto(productRepository.save(entity));
    }

    @Override
    public ProductDto deactivateProduct(Long id) {
        ProductEntity entity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setState(ProductState.DEACTIVATE);
        return toDto(productRepository.save(entity));
    }

    @Override
    public ProductDto setQuantityState(Long id, QuantityState quantityState) {
        ProductEntity entity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setQuantityState(quantityState);
        return toDto(productRepository.save(entity));
    }

    private ProductDto toDto(ProductEntity entity) {
        return ProductDto.builder()
                .id(entity.getId()).name(entity.getName()).description(entity.getDescription())
                .price(entity.getPrice()).category(entity.getCategory())
                .quantityState(entity.getQuantityState()).state(entity.getState()).build();
    }

    private ProductEntity toEntity(ProductDto dto) {
        return ProductEntity.builder()
                .id(dto.getId()).name(dto.getName()).description(dto.getDescription())
                .price(dto.getPrice()).category(dto.getCategory())
                .quantityState(dto.getQuantityState()).state(dto.getState()).build();
    }
}
