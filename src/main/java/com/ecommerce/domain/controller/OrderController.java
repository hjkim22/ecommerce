package com.ecommerce.domain.controller;

import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.order.OrderCreateDto;
import com.ecommerce.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
