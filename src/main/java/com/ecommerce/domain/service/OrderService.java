package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.order.OrderCreateDto;
import com.ecommerce.domain.entity.CartEntity;
import com.ecommerce.domain.entity.OrderEntity;
import com.ecommerce.domain.entity.OrderItemEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;

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
