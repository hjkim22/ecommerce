package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.order.OrderCreateDto;
import com.ecommerce.domain.dto.order.OrderDto;
import com.ecommerce.domain.dto.order.OrderUpdateDto;
import com.ecommerce.domain.entity.CartEntity;
import com.ecommerce.domain.entity.OrderEntity;
import com.ecommerce.domain.entity.OrderItemEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.OrderRepository;
import com.ecommerce.domain.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;

  // 주문 생성
  @Transactional // 주문 생성이랑 장바구니 비우기 때문에 주문 생성 실패 시 장바구니가 안비워지게.
  public OrderCreateDto.Response createOrder(Long customerId, OrderCreateDto.Request request) {
    // 소유권 확인
    CartEntity cart = validateCartOwnership(customerId, request.getCartId());
    // 상품이 있는지 확인
    validateCartNotEmpty(cart);

    // 항목 생성
    List<OrderItemEntity> orderItems = createOrderItem(cart);
    OrderEntity order = buildOrder(request, cart, orderItems);
    order.addOrderItems(orderItems);
    orderRepository.save(order);

    clearCart(cart); // 주문했으니 비우기

    return new OrderCreateDto.Response(request.getCartId(), order.getStatus(), "주문 완료");
  }

  // orderId로 조회
  public OrderDto getOrderById(Long orderId) {
    return OrderDto.fromEntity(findOrderById(orderId));
  }

  // customerId로 조회
  public List<OrderDto> getOrdersByCustomerId(Long customerId) {
    validateCustomerExists(customerId);
    List<OrderEntity> orders = orderRepository.findByCustomerId(customerId);

    if (orders.isEmpty()) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }
    return orders.stream().map(OrderDto::fromEntity).toList();
  }

  // 상태별 조회
  public List<OrderDto> getOrderByStatus(OrderStatus status) {
    return orderRepository.findByStatus(status).stream().map(OrderDto::fromEntity).toList();
  }

  // 사용자 주문 취소
  @Transactional
  public OrderDto cancelOrder(Long orderId) {
    OrderEntity order = findOrderById(orderId);
    validateOrderCancellable(order);

    order.setStatus(OrderStatus.CANCELED);
    restoreStock(order); // 재고 복구
    return OrderDto.fromEntity(order);
  }

  // 상태 변경
  @Transactional
  public OrderDto changeOrderStatus(Long orderId, OrderStatus newStatus) {
    OrderEntity order = findOrderById(orderId);

    // 상태별 변경 가능 여부 확인
    switch (order.getStatus()) {
      case PENDING ->
          validateAndSetStatus(order, newStatus, OrderStatus.SHIPPED, OrderStatus.CANCELED);
      case SHIPPED -> validateAndSetStatus(order, newStatus, OrderStatus.DELIVERED);
      default -> throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
    }

    return OrderDto.fromEntity(order);
  }

  // 배송지 수정
  @Transactional
  public OrderDto updateDeliveryAddress(Long orderId, OrderUpdateDto request) {
    OrderEntity order = findOrderById(orderId);
    validateOrderModifiable(order);

    order.setDeliveryAddress(request.getDeliveryAddress());
    return OrderDto.fromEntity(order);
  }

  // ========================== 헬퍼메서드 ==========================

  // 장바구니 소유권 확인
  private CartEntity validateCartOwnership(Long customerId, Long cartId) {
    CartEntity cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    if (!cart.getCustomer().getId().equals(customerId)) {
      throw new CustomException(ErrorCode.INVALID_CUSTOMER_ACCESS);
    }
    return cart;
  }

  // 장바구니 엠티 확인
  private void validateCartNotEmpty(CartEntity cart) {
    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }
  }

  // 주문 항목 생성, 재고량 반영
  private List<OrderItemEntity> createOrderItem(CartEntity cart) {
    return cart.getCartItems().stream()
        .map(cartItem -> {
          ProductEntity product = cartItem.getProduct();
          checkStockAvailability(product, cartItem.getQuantity());
          product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
          return createOrderItem(product, cartItem.getQuantity());
        })
        .toList();
  }

  // 장바구니 비우기
  private void clearCart(CartEntity cart) {
    cart.getCartItems().clear();
    cartRepository.save(cart);
  }

  // 대기중 상태아니면 취소불가
  private void validateOrderCancellable(OrderEntity order) {
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
    }
  }

  // 대기중상태 아니면 수정 불가
  private void validateOrderModifiable(OrderEntity order) {
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
    }
  }

  // 주문 상태 변경 가능 여부 확인 및 상태 업데이트
  private void validateAndSetStatus(OrderEntity order, OrderStatus newStatus,
      OrderStatus... allowedStatuses) {
    if (List.of(allowedStatuses).contains(newStatus)) {
      if (newStatus == OrderStatus.CANCELED) {
        restoreStock(order); // 취소 시 재고 복구
      }
      order.setStatus(newStatus);
    } else {
      throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
    }
  }

  // 취소 시 재고량 복구
  private void restoreStock(OrderEntity order) {
    for (OrderItemEntity orderItem : order.getOrderItems()) {
      ProductEntity product = orderItem.getProduct();
      Integer quantityToRestore = orderItem.getQuantity();

      // 재고 복구: 상품의 재고에 수량 더하기
      product.setStockQuantity(product.getStockQuantity() + quantityToRestore);

      // 재고 정보 업데이트
      productRepository.save(product);
    }
  }

  // 재고량 확인
  private void checkStockAvailability(ProductEntity product, Integer quantity) {
    if (product.getStockQuantity() < quantity) {
      throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
    }
  }

  // 주문 항목 생성
  private OrderItemEntity createOrderItem(ProductEntity product, Integer quantity) {
    return OrderItemEntity.builder()
        .product(product)
        .quantity(quantity)
        .price(product.getPrice())
        .build();
  }

  // 주문 엔티티 생성 메서드
  private static OrderEntity buildOrder(OrderCreateDto.Request request, CartEntity cart,
      List<OrderItemEntity> orderItems) {
    return OrderEntity.builder()
        .customer(cart.getCustomer())
        .status(OrderStatus.PENDING) // 기본값 대기중
        .deliveryAddress(request.getDeliveryAddress())
        .orderItems(orderItems)
        .cart(cart)
        .build();
  }

  private OrderEntity findOrderById(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  private void validateCustomerExists(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }
}
