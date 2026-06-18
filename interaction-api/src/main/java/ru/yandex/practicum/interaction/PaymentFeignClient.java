package ru.yandex.practicum.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment")
public interface PaymentFeignClient {
    @PostMapping("/api/v1/payment/totalCost")
    Double getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/productCost")
    Double productCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto order);
}
