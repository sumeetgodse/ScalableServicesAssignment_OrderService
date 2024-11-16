package com.scalable.shoppify.order_service.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderResponse {
    int status;
    String message;
}
