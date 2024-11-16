package com.scalable.shoppify.order_service.service;

import com.scalable.shoppify.order_service.models.Order;
import com.scalable.shoppify.order_service.models.PaymentDetail;
import com.scalable.shoppify.order_service.repositories.OrderRepository;

import com.scalable.shoppify.order_service.models.OrderRequest;
import com.scalable.shoppify.order_service.models.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        try {
            //Generating Random Integer Id
            order.setOrderId(generateOrderId());

            //Setting from Customer Request
            order.setUserId(orderRequest.getUserId());
            order.setPrice(orderRequest.getPrice());
            order.setOrderItems(orderRequest.getOrderItems());
            order.setPaymentDetail(orderRequest.getPaymentDetail());

            // Saving Order to Database
            orderRepository.save(order);

            // Making Payment via PaymentService
            PaymentDetail paymentDetail = orderRequest.getPaymentDetail();

            if (paymentDetail.getCardNumber() != null) {

                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("cardNum", paymentDetail.getCardNumber());
                requestPayload.put("cardExpiry", paymentDetail.getCardExpiry());
                requestPayload.put("cvv", paymentDetail.getCvv());
                requestPayload.put("orderId", order.getOrderId());
                requestPayload.put("userId", order.getUserId());
                makePostCall(requestPayload, "http://localhost:3005/pay/card");
            } else {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("upiNumber", paymentDetail.getUpiNumber());
                requestPayload.put("orderId", order.getOrderId());
                requestPayload.put("userId", order.getUserId());
                makePostCall(requestPayload, "http://localhost:3005/pay/upi");
            }

            // Notifying User via NotificationService
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("userId", order.getUserId());
            requestPayload.put("message", "Your order with order id " + order.getOrderId() + " is placed successfully");
//            makePostCall(requestPayload, "http://localhost:8083/notify");

            return OrderResponse.builder().message("Order Placed Successfully").status(HttpStatus.OK.value()).build();

        } catch (Exception exception) {
            log.error("Error occurred while placing order: {}", exception.getMessage());
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("userId", order.getUserId());
            requestPayload.put("message", "Error occurred while placing order " + exception.getMessage());
//            makePostCall(requestPayload, "http://localhost:8083/notify");
            return OrderResponse.builder().message("Error occurred while placing order").status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }
    }

    private void makePostCall(Object requestPayload, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Object> entity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        log.info("Response: {}", response.getBody());
    }

    private int generateOrderId() {
        // Generate a random number between 101 and 500 (inclusive)
        Random random = new Random();
        int min = 101;
        int max = 500;
        int randomNumber = random.nextInt(max - min + 1) + min;
        if (orderRepository.findByOrderId(randomNumber) != null) {
            return generateOrderId();
        }
        return randomNumber;
    }

}
