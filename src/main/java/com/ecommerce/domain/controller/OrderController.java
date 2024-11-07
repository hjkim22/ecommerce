package com.ecommerce.domain.controller;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.order.OrderCreateDto;
import com.ecommerce.domain.dto.order.OrderDto;
import com.ecommerce.domain.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderController {

  private final OrderService orderService;

  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PostMapping
  public ResponseEntity<OrderCreateDto.Response> createOrder(
      @Valid @RequestBody OrderCreateDto.Request request,
      @JwtToken Long customerId) {

    OrderCreateDto.Response response = orderService.createOrder(customerId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("{orderId}")
  public ResponseEntity<OrderDto> getOrderById(@PathVariable("orderId") Long orderId) {
    OrderDto order = orderService.getOrderById(orderId);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/customerId/{customerId}")
  public ResponseEntity<List<OrderDto>> getOrderByCustomerId(
      @PathVariable("customerId") Long customerId) {
    List<OrderDto> orders = orderService.getOrdersByCustomerId(customerId);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<OrderDto>> getOrderByStatus(
      @PathVariable("status") OrderStatus status) {
    List<OrderDto> orders = orderService.getOrderByStatus(status);
    return ResponseEntity.ok(orders);
  }
}
