package ru.yandex.practicum.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "delivery")
public interface DeliveryFeignClient {
    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@RequestBody DeliveryDto delivery);

    @PostMapping("/api/v1/delivery/cost")
    Double deliveryCost(@RequestBody OrderDto order);
}
