package com.ecommerce.domain.controller;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.order.OrderCreateDto;
import com.ecommerce.domain.dto.order.OrderDto;
import com.ecommerce.domain.dto.order.OrderUpdateDto;
import com.ecommerce.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    log.info("주문 생성 요청");
    OrderCreateDto.Response response = orderService.createOrder(customerId, request);
    log.info("주문 생성 완료 - cart ID: {}", request.getCartId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("{orderId}")
  public ResponseEntity<OrderDto> getOrderById(@PathVariable("orderId") Long orderId) {
    log.info("주문 조회 요청 - 주문 ID: {}", orderId);
    OrderDto order = orderService.getOrderById(orderId);
    return ResponseEntity.ok(order);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/orders")
  public ResponseEntity<Page<OrderDto>> getOrderByCustomerId(@RequestParam Long customerId,
      Pageable pageable) {
    log.info("주문 목록 조회 요청 - 사용자 ID: {}", customerId);
    Page<OrderDto> orders = orderService.getOrdersByCustomerId(customerId, pageable);
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/status/{status}")
  public ResponseEntity<Page<OrderDto>> getOrderByStatus(@PathVariable("status") OrderStatus status,
      Pageable pageable) {
    log.info("주문 상태별 조회 요청 - 상태: {}", status);
    Page<OrderDto> orders = orderService.getOrderByStatus(status, pageable);
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/findAll")
  public ResponseEntity<Page<OrderDto>> getOrders(Pageable pageable) {
    log.info("모든 주문 목록 조회");
    Page<OrderDto> orders = orderService.getOrders(pageable);
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CUSTOMER')")
  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<OrderDto> cancelOrder(@PathVariable("orderId") Long orderId,
      @JwtToken Long customerId) {
    log.info("주문 취소 요청 - 주문 ID: {}", orderId);
    OrderDto order = orderService.cancelOrder(orderId, customerId);
    return ResponseEntity.ok(order);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PatchMapping("/{orderId}/status/{status}")
  public ResponseEntity<OrderDto> changeOrderStatus(
      @PathVariable("orderId") Long orderId,
      @PathVariable("status") OrderStatus status) {
    log.info("주문 상태 변경 요청 - 주문 ID: {}, 상태: {}", orderId, status);
    OrderDto orderStatus = orderService.changeOrderStatus(orderId, status);
    return ResponseEntity.ok(orderStatus);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CUSTOMER')")
  @PatchMapping("/{orderId}/deliveryAddress")
  public ResponseEntity<OrderDto> updateDeliveryAddress(
      @PathVariable("orderId") Long orderId,
      @Valid @RequestBody OrderUpdateDto orderUpdateDto,
      @JwtToken Long customerId) {
    log.info("주문 배송지 변경 요청 - 주문 ID: {}", orderId);
    OrderDto updatedOrder = orderService.updateDeliveryAddress(orderId, orderUpdateDto, customerId);
    return ResponseEntity.ok(updatedOrder);
  }
}
