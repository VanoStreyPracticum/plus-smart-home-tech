package ru.yandex.practicum.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.interaction.ProductCategory;
import ru.yandex.practicum.store.model.ProductEntity;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findByCategory(ProductCategory category, Pageable pageable);
}
