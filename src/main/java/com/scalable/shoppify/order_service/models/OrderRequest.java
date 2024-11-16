package com.scalable.shoppify.order_service.models;


import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private Integer userId;
    private Double price;
    private List<OrderItem> orderItems;
    private PaymentDetail paymentDetail;


}
