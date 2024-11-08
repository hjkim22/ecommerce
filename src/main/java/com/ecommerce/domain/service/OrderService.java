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
    CartEntity cart = cartRepository.findById(request.getCartId())
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    // 장바구니, 사용자 일치 체크
    if (!cart.getCustomer().getId().equals(customerId)) {
      throw new CustomException(ErrorCode.INVALID_CUSTOMER_ACCESS);
    }
    // 장바구니 비어있는지
    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }

    List<OrderItemEntity> orderItems = cart.getCartItems().stream()
        .map(cartItem -> {
          ProductEntity product = cartItem.getProduct();

          if (product.getStockQuantity() < cartItem.getQuantity()) {
            throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
          }

          product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

          return createOrderItem(product, cartItem.getQuantity());
        })
        .toList();

    OrderEntity order = createOrderEntity(request, cart, orderItems);
    order.addOrderItems(orderItems);
    orderRepository.save(order);

    cart.getCartItems().clear(); // 주문 완료 시 장바구니 clear
    cartRepository.save(cart); // 더티체킹으로 생략 가능한지 확인

    return new OrderCreateDto.Response(request.getCartId(), order.getStatus(), "주문 완료");
  }

  // orderId로 조회
  public OrderDto getOrderById(Long orderId) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    return OrderDto.fromEntity(order);
  }

  // customerId로 조회
  public List<OrderDto> getOrdersByCustomerId(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    List<OrderEntity> orders = orderRepository.findByCustomerId(customerId);
    if (orders.isEmpty()) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }

    return orders.stream().map(OrderDto::fromEntity).toList();
  }

  // 상태별 조회
  public List<OrderDto> getOrderByStatus(OrderStatus status) {
    List<OrderEntity> orders = orderRepository.findByStatus(status);
    return orders.stream().map(OrderDto::fromEntity).toList();
  }

  // 사용자 주문 취소
  @Transactional
  public OrderDto cancelOrder(Long orderId) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

    // 대기중인 경우만 취소 가능
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
    }
    order.setStatus(OrderStatus.CANCELED);

    // 상품 재고 복구
    for (OrderItemEntity orderItem : order.getOrderItems()) {
      ProductEntity product = orderItem.getProduct();
      product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
    }
    orderRepository.save(order);

    return OrderDto.fromEntity(order);
  }

  // 상태 변경
  @Transactional
  public OrderDto changeOrderStatus(Long orderId, OrderStatus newStatus) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

    // 변경 및 재고 복구
    if (order.getStatus().equals(OrderStatus.PENDING)) { // == 쓸지 이퀄쓸지 확인
      // 대기중은 배송중이나 취소로만 변경가능
      if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED) {
        if (newStatus == OrderStatus.CANCELED) {
          restoreStock(order);
        }
        order.setStatus(newStatus);
      } else {
        throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
      }
    } else if (order.getStatus() == OrderStatus.SHIPPED) {
      // 배송중일떄는 배송완료로만 변경가능
      if (newStatus == OrderStatus.DELIVERED) {
        order.setStatus(newStatus);
      } else {
        throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
      }
    } else {
      // 이미 배송완료나 취소 상태인 경우 변경 불가
      throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
    }
    orderRepository.save(order);

    return OrderDto.fromEntity(order);
  }

  // 배송지 수정
  @Transactional
  public OrderDto updateDeliveryAddress(Long orderId, OrderUpdateDto request) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
    }

    order.setDeliveryAddress(request.getDeliveryAddress());
    orderRepository.save(order);
    return OrderDto.fromEntity(order);
  }

  // 취소 시 재고량 복구
  private void restoreStock(OrderEntity order) {
    for (OrderItemEntity orderItem : order.getOrderItems()) {
      ProductEntity product = orderItem.getProduct();
      int quantityToRestore = orderItem.getQuantity();

      // 재고 복구: 상품의 재고에 수량 더하기
      product.setStockQuantity(product.getStockQuantity() + quantityToRestore);

      // 재고 정보 업데이트
      productRepository.save(product);
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

  // 엔티티 생성 메서드
  private static OrderEntity createOrderEntity(OrderCreateDto.Request request, CartEntity cart,
      List<OrderItemEntity> orderItems) {
    return OrderEntity.builder()
        .customer(cart.getCustomer())
        .status(OrderStatus.PENDING) // 기본값 대기중
        .deliveryAddress(request.getDeliveryAddress())
        .orderItems(orderItems)
        .cart(cart)
        .build();
  }
}
