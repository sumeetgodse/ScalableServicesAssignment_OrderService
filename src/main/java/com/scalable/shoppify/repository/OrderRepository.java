package com.scalable.shoppify.repository;

import com.scalable.shoppify.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository <Order,Integer> {
    Order findByOrderId(int orderId);
}
