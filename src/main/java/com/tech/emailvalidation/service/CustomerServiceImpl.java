package com.tech.emailvalidation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.emailvalidation.dto.ApiResponse;
import com.tech.emailvalidation.dto.CustomerRequest;
import com.tech.emailvalidation.dto.OrderRequest;
import com.tech.emailvalidation.entity.Customer;
import com.tech.emailvalidation.entity.Order;
import com.tech.emailvalidation.exception.EmailValidationException;
import com.tech.emailvalidation.exception.ResourceNotFoundException;
import com.tech.emailvalidation.repository.CustomerRepository;
import com.tech.emailvalidation.repository.OrderRepository;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final EmailValidationService emailValidationService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               OrderRepository orderRepository,
                               EmailValidationService emailValidationService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.emailValidationService = emailValidationService;
    }

    @Override
    public Customer createCustomer(CustomerRequest request) {
        String email = request.getEmail().trim();

        // 1. Check duplicate email in database
        if (customerRepository.existsByEmail(email)) {
            throw new EmailValidationException("Customer with email '" + email + "' already exists");
        }

        // 2. Validate email using validateEmail
        ApiResponse validationResponse = emailValidationService.validateEmail(email);
        if (validationResponse == null || !validationResponse.isValid()) {
            String details = (validationResponse != null && validationResponse.getMessage() != null)
                    ? validationResponse.getMessage()
                    : "Email is not valid or deliverable";
            throw new EmailValidationException("Email validation failed for '" + email + "': " + details);
        }

        // 3. Build customer entity
        Customer customer = Customer.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .isEmailValid(true)
                .emailValidationStatus("VALID")
                .build();

        // 4. Attach initial orders if provided
        if (request.getOrders() != null && !request.getOrders().isEmpty()) {
            for (OrderRequest ordReq : request.getOrders()) {
                Order order = Order.builder()
                        .orderNumber(ordReq.getOrderNumber().trim())
                        .productName(ordReq.getProductName() != null ? ordReq.getProductName().trim() : null)
                        .amount(ordReq.getAmount())
                        .build();
                customer.addOrder(order);
            }
        }

        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer existingCustomer = getCustomerById(id);
        String newEmail = request.getEmail().trim();

        // If email has changed, re-validate
        if (!existingCustomer.getEmail().equalsIgnoreCase(newEmail)) {
            if (customerRepository.existsByEmailAndIdNot(newEmail, id)) {
                throw new EmailValidationException("Email '" + newEmail + "' is already in use by another customer");
            }

            ApiResponse validationResponse = emailValidationService.validateEmail(newEmail);
            if (validationResponse == null || !validationResponse.isValid()) {
                String details = (validationResponse != null && validationResponse.getMessage() != null)
                        ? validationResponse.getMessage()
                        : "Email is not valid or deliverable";
                throw new EmailValidationException("Email validation failed for '" + newEmail + "': " + details);
            }

            existingCustomer.setEmail(newEmail);
            existingCustomer.setIsEmailValid(true);
            existingCustomer.setEmailValidationStatus("VALID");
        }

        existingCustomer.setName(request.getName().trim());
        existingCustomer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);

        return customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer existingCustomer = getCustomerById(id);
        customerRepository.delete(existingCustomer);
    }

    @Override
    public Order addOrderToCustomer(Long customerId, OrderRequest orderRequest) {
        Customer customer = getCustomerById(customerId);

        Order order = Order.builder()
                .orderNumber(orderRequest.getOrderNumber().trim())
                .productName(orderRequest.getProductName() != null ? orderRequest.getProductName().trim() : null)
                .amount(orderRequest.getAmount())
                .build();

        customer.addOrder(order);
        customerRepository.save(customer);

        return order;
    }
}
