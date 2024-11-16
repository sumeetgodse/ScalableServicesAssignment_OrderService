package com.scalable.shoppify.order_service.controller;

import com.scalable.shoppify.order_service.models.Order;
import com.scalable.shoppify.order_service.models.OrderRequest;
import com.scalable.shoppify.order_service.models.OrderResponse;
import com.scalable.shoppify.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


    @PostMapping
    @Operation(summary = "Place an order", description = "Place a new order",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"userId\": 1,\n" +
                                            "  \"price\": 100,\n" +
                                            "  \"orderItems\": [\n" +
                                            "    {\n" +
                                            "      \"orderItemId\": 1,\n" +
                                            "      \"menuItemId\": 2,\n" +
                                            "      \"quantity\": 4,\n" +
                                            "      \"itemPrice\": 25\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"paymentDetail\": {\n" +
                                            "    \"cardNumber\": 123456789,\n" +
                                            "    \"cardExpiry\": \"2024-10-12\",\n" +
                                            "    \"cvv\": 123\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order successfully placed"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse placedOrder = orderService.placeOrder(orderRequest);
        return new ResponseEntity<>(placedOrder, HttpStatus.CREATED);
    }
}