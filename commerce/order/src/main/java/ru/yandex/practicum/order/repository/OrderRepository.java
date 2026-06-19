package ru.yandex.practicum.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.model.OrderEntity;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByUserName(String userName);
}
