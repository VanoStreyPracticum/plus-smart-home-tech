package ru.yandex.practicum.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.payment.model.PaymentEntity;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}
