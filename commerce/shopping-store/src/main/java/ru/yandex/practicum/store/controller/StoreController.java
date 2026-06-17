package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.*;
import ru.yandex.practicum.store.model.ProductEntity;
import ru.yandex.practicum.store.repository.ProductRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class StoreController {

    private final ProductRepository productRepository;

    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam String category,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "productName,asc") String[] sort) {
        ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
        String[] sortParams = sort[0].split(",");
        String property = sortParams[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, property));
        return productRepository.findByCategory(productCategory, pageRequest)
                .map(this::toDto);
    }

    @PutMapping
    public ProductDto createNewProduct(@RequestBody ProductDto productDto) {
        ProductEntity entity = toEntity(productDto);
        entity.setProductId(UUID.randomUUID());
        entity = productRepository.save(entity);
        return toDto(entity);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        ProductEntity entity = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setProductName(productDto.getProductName());
        entity.setDescription(productDto.getDescription());
        entity.setImageSrc(productDto.getImageSrc());
        entity.setPrice(productDto.getPrice());
        entity.setCategory(productDto.getProductCategory());
        entity.setQuantityState(productDto.getQuantityState());
        entity.setState(productDto.getProductState());
        entity = productRepository.save(entity);
        return toDto(entity);
    }

    @PostMapping("/removeProductFromStore")
    public Boolean removeProductFromStore(@RequestBody String productId) {
        String clean = productId.replaceAll("^\"|\"$", "");
        UUID id = UUID.fromString(clean);
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setState(ProductState.DEACTIVATE);
        productRepository.save(entity);
        return true;
    }

    @PostMapping("/quantityState")
    public Boolean setProductQuantityState(@RequestParam UUID productId,
                                           @RequestParam QuantityState quantityState) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setQuantityState(quantityState);
        productRepository.save(entity);
        return true;
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(entity);
    }

    private ProductDto toDto(ProductEntity entity) {
        return ProductDto.builder()
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .description(entity.getDescription())
                .imageSrc(entity.getImageSrc())
                .quantityState(entity.getQuantityState())
                .productState(entity.getState())
                .productCategory(entity.getCategory())
                .price(entity.getPrice())
                .build();
    }

    private ProductEntity toEntity(ProductDto dto) {
        return ProductEntity.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .price(dto.getPrice())
                .category(dto.getProductCategory())
                .quantityState(dto.getQuantityState())
                .state(dto.getProductState())
                .build();
    }
}
