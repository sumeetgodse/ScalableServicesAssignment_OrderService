package com.scalable.shoppify.order_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Document(collection = "order_details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private Integer orderId;
    private Integer userId;
    private Double price;
    private List<OrderItem> orderItems;
    private String orderStatus;

}


