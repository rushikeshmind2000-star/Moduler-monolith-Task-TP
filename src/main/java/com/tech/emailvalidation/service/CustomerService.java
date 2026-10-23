package com.tech.emailvalidation.service;

import java.util.List;

import com.tech.emailvalidation.dto.CustomerRequest;
import com.tech.emailvalidation.dto.OrderRequest;
import com.tech.emailvalidation.entity.Customer;
import com.tech.emailvalidation.entity.Order;

public interface CustomerService {

    Customer createCustomer(CustomerRequest request);

    Customer getCustomerById(Long id);

    List<Customer> getAllCustomers();

    Customer updateCustomer(Long id, CustomerRequest request);

    void deleteCustomer(Long id);

    Order addOrderToCustomer(Long customerId, OrderRequest orderRequest);
}
