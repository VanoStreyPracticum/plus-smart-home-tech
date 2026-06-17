package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
                                        @PageableDefault(page = 0, size = 20, sort = "productName", direction = Sort.Direction.ASC) Pageable pageable) {
        ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
        return productRepository.findByCategory(productCategory, pageable)
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
        String clean = productId.replaceAll("^\"|\"$", "").trim();
        UUID id = UUID.fromString(clean);
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setState(ProductState.DEACTIVATE);
        productRepository.save(entity);
        return true;
    }

    @PostMapping("/quantityState")
    public Boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        ProductEntity entity = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        entity.setQuantityState(request.getQuantityState());
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
