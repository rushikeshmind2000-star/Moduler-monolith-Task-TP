package com.tech.emailvalidation.service;

import java.util.List;

import com.tech.emailvalidation.dto.OrderRequest;
import com.tech.emailvalidation.entity.Order;

public interface OrderService {

    Order createOrder(OrderRequest request);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    List<Order> getOrdersByCustomerId(Long customerId);

    Order updateOrder(Long id, OrderRequest request);

    void deleteOrder(Long id);
}
