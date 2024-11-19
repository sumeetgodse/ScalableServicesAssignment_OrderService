package com.scalable.shoppify.service;

import com.scalable.shoppify.model.OrderRequest;
import com.scalable.shoppify.model.OrderResponse;
import com.scalable.shoppify.model.PaymentDetail;
import com.scalable.shoppify.model.Order;
import com.scalable.shoppify.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository, KafkaProducerService kafkaProducerService, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.restTemplate = restTemplate;
    }

    public OrderResponse<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            return OrderResponse.<List<Order>>builder().result(orders).status(HttpStatus.OK).message("Successfully Retrieved Orders").build();
        } catch (Exception exception) {
            log.error(exception.getMessage());
            return OrderResponse.<List<Order>>builder()
                    .result(Collections.EMPTY_LIST)
                    .message("Failed to retrieve Orders" + exception.getMessage())
                    .status(HttpStatus.OK).build();
        }
    }

    public OrderResponse<Order> placeOrder(OrderRequest orderRequest) {
        Order order = new Order();

        Map<String, Object> notificationsPayload = new HashMap<>();

        String userMessage = "";
        HttpStatus httpStatus = HttpStatus.CREATED;

        try {
            //Generating Random Integer Id for Order Id
            order.setOrderId(generateOrderId());

            //Setting from Customer Request
            order.setUserId(orderRequest.getUserId());
            order.setPrice(orderRequest.getPrice());
            order.setOrderItems(orderRequest.getOrderItems());

            // Making Payment via PaymentService
            PaymentDetail paymentDetail = orderRequest.getPaymentDetail();

            Map<String, String> response;

            if (paymentDetail.getCardNumber() != null) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("cardNum", paymentDetail.getCardNumber());
                requestPayload.put("cardExpiry", paymentDetail.getCardExpiry());
                requestPayload.put("cvv", paymentDetail.getCvv());
                requestPayload.put("orderId", order.getOrderId());
                requestPayload.put("userId", order.getUserId());
                response = makePostCall(requestPayload, "http://localhost:3005/pay/card");
            } else {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("upiNumber", paymentDetail.getUpiNumber());
                requestPayload.put("orderId", order.getOrderId());
                requestPayload.put("userId", order.getUserId());
                response = makePostCall(requestPayload, "http://localhost:3005/pay/upi");
            }

            String status = response.get("message");

            // Check if Payment was success
            if ("Payment FAILED!".equals(status)) {
                throw new RuntimeException(response.get("errorMessage"));
            } else {
                // Update Order Status
                order.setOrderStatus("PLACED");
                userMessage = "Your order with order id " + order.getOrderId() + " is placed successfully";
                //Mark Success Notification
                notificationsPayload.put("message", userMessage);
            }

        } catch (Exception exception) {
            //Log the error message
            log.error(exception.getMessage());

            //Update Order Status
            order.setOrderStatus("FAILED_TO_PROCESS");

            // Set User Message
            userMessage = "Failed to process order - " + exception.getMessage();

            // Set HTTP Status
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            //Mark Failure Notification
            notificationsPayload.put("message", userMessage);

        } finally {
            // Saving Order to Database
            orderRepository.save(order);

            // Update Inventory
            kafkaProducerService.sendMessage(order);

            notificationsPayload.put("userId", order.getUserId());
            notificationsPayload.put("orderId", order.getOrderId());

            //Sending Notifications
            makePostCall(notificationsPayload, "http://localhost:3006/notify");

        }

        //Return Response
        return OrderResponse.<Order>builder().result(order).message(userMessage).status(httpStatus).build();
    }

    private Map<String, String> makePostCall(Object requestPayload, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Object> entity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
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
