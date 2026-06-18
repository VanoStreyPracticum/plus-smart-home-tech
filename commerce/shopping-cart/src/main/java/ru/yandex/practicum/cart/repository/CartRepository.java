package ru.yandex.practicum.cart.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.cart.model.CartEntity;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserName(String userName);
}
