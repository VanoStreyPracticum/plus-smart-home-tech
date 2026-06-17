package ru.yandex.practicum.store.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.interaction.ProductCategory;
import ru.yandex.practicum.store.model.ProductEntity;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByCategory(ProductCategory category);
}
