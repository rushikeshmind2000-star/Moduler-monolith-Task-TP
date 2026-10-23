package com.tech.emailvalidation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.emailvalidation.dto.OrderRequest;
import com.tech.emailvalidation.entity.Customer;
import com.tech.emailvalidation.entity.Order;
import com.tech.emailvalidation.exception.EmailValidationException;
import com.tech.emailvalidation.exception.ResourceNotFoundException;
import com.tech.emailvalidation.repository.CustomerRepository;
import com.tech.emailvalidation.repository.OrderRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Order createOrder(OrderRequest request) {
        if (request.getCustomerId() == null) {
            throw new EmailValidationException("Customer ID is required to create an order");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Order order = Order.builder()
                .orderNumber(request.getOrderNumber().trim())
                .productName(request.getProductName() != null ? request.getProductName().trim() : null)
                .amount(request.getAmount())
                .build();

        customer.addOrder(order);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public Order updateOrder(Long id, OrderRequest request) {
        Order existingOrder = getOrderById(id);

        if (request.getOrderNumber() != null && !request.getOrderNumber().isBlank()) {
            existingOrder.setOrderNumber(request.getOrderNumber().trim());
        }

        if (request.getProductName() != null) {
            existingOrder.setProductName(request.getProductName().trim());
        }

        if (request.getAmount() != null) {
            existingOrder.setAmount(request.getAmount());
        }

        // If customer reassignment is requested
        if (request.getCustomerId() != null && !request.getCustomerId().equals(existingOrder.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            
            existingOrder.getCustomer().removeOrder(existingOrder);
            newCustomer.addOrder(existingOrder);
        }

        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        Order existingOrder = getOrderById(id);
        if (existingOrder.getCustomer() != null) {
            existingOrder.getCustomer().removeOrder(existingOrder);
        }
        orderRepository.delete(existingOrder);
    }
}
