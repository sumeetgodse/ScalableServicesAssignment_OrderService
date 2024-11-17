package com.scalable.shoppify.order_service.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class OrderResponse<T> {
    HttpStatus status;
    T result;
    String message;
}
