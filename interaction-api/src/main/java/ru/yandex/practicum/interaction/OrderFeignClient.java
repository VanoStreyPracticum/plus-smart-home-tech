package ru.yandex.practicum.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(name = "order")
public interface OrderFeignClient {
    @PostMapping("/api/v1/order/delivery")
    void delivery(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    void deliveryFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment")
    void payment(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/failed")
    void paymentFailed(@RequestBody String orderId);
}
