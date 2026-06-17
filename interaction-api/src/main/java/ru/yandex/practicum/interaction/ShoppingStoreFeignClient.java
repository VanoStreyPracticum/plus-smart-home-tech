package ru.yandex.practicum.interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreFeignClient {
    @GetMapping("/products")
    List<ProductDto> getProducts(@RequestParam(name = "category", required = false) ProductCategory category);
    
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
    
    @PostMapping("/products")
    ProductDto addProduct(@RequestBody ProductDto productDto);
    
    @PutMapping("/products/{id}")
    ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto);
    
    @DeleteMapping("/products/{id}")
    ProductDto deactivateProduct(@PathVariable Long id);
    
    @PatchMapping("/products/{id}/quantity")
    ProductDto setQuantityState(@PathVariable Long id, @RequestParam QuantityState quantityState);
}
