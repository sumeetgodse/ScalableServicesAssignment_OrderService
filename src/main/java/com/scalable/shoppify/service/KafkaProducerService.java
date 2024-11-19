package com.scalable.shoppify.service;

import com.scalable.shoppify.model.Order;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Value("${topic.name}")
    private String topicName;

    public KafkaProducerService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Order order) {
        kafkaTemplate.send(topicName, order);
    }
}

