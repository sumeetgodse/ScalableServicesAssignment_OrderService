package com.scalable.shoppify.order_service.repositories;

import com.scalable.shoppify.order_service.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository <Order,Integer> {
    Order findByOrderId(int randomNumber);
}
