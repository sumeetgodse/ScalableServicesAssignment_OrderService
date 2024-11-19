package com.scalable.shoppify.model;


import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String userId;
    private Double price;
    private List<OrderItem> orderItems;
    private PaymentDetail paymentDetail;


}
