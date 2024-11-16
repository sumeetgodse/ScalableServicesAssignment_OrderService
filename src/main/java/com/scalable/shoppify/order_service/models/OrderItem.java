package com.scalable.shoppify.order_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    private int orderItemId;
    private int quantity;
    private double price;

}
