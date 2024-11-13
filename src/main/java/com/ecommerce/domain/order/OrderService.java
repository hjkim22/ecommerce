package com.ecommerce.domain.order;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.cart.Cart;
import com.ecommerce.domain.cart.CartRepository;
import com.ecommerce.domain.member.MemberRepository;
import com.ecommerce.domain.order.dto.OrderCreateDto;
import com.ecommerce.domain.order.dto.OrderDto;
import com.ecommerce.domain.order.dto.OrderUpdateDto;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;

  /**
   * 주문 생성
   *
   * @param customerId 사용자 ID
   * @param request    주문 생성 요청 DTO
   * @return 주문 생성 응답 DTO
   */
  @Transactional
  public OrderCreateDto.Response createOrder(Long customerId, OrderCreateDto.Request request) {
    Cart cart = validateCartOwnership(customerId, request.getCartId());
    validateCartNotEmpty(cart);

    List<OrderItem> orderItems = createOrderItemsFromCart(cart);
    Order order = buildOrder(request, cart, orderItems);
    order.addOrderItems(orderItems);

    orderRepository.save(order);
    clearCart(cart);

    return new OrderCreateDto.Response(request.getCartId(), order.getStatus(), "주문 완료");
  }

  /**
   * 주문 정보 조회
   *
   * @param orderId 주문 ID
   * @return 주문 DTO
   */
  public OrderDto getOrderById(Long orderId) {
    return OrderDto.fromEntity(findOrderById(orderId));
  }

  /**
   * 주문 정보 조회
   *
   * @param customerId 사용자 ID
   * @return 주문 DTO 리스트
   */
  public Page<OrderDto> getOrdersByCustomerId(Long customerId, Pageable pageable) {
    validateCustomerExists(customerId);
    Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);

    if (orders.isEmpty()) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }

    return orders.map(OrderDto::fromEntity);
  }

  /**
   * 주문 정보 조회
   *
   * @param status 주문 상태
   * @return 주문 DTO 리스트
   */
  public Page<OrderDto> getOrderByStatus(OrderStatus status, Pageable pageable) {
    Page<Order> orders = orderRepository.findByStatus(status, pageable);
    return orders.map(OrderDto::fromEntity);
  }

  /**
   * 전체 주문 리스트 조회
   *
   * @param pageable 페이징 정보
   * @return 전체 주문 목록 - 최신순
   */
  public Page<OrderDto> getAllOrders(Pageable pageable) {
    Pageable sortedByCreatedAtDesc = PageRequest.of(
        pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
    Page<Order> orders = orderRepository.findAll(sortedByCreatedAtDesc);

    return orders.map(OrderDto::fromEntity);
  }

  /**
   * 사용자 주문 취소
   *
   * @param orderId 주문 ID
   * @return 취소된 주문 DTO
   */
  @Transactional
  public OrderDto cancelOrder(Long orderId, Long customerId) {
    Order order = findOrderById(orderId);

    validateCustomerAuthorization(customerId, order);
    validateOrderCancellable(order);

    order.setStatus(OrderStatus.CANCELED);
    restoreStock(order);

    return OrderDto.fromEntity(order);
  }

  /**
   * 주문 상태 변경
   *
   * @param orderId   주문 ID
   * @param newStatus 새로운 주문 상태
   * @return 변경된 주문 DTO
   */
  @Transactional
  public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = findOrderById(orderId);

    switch (order.getStatus()) {
      case PENDING -> validateStatusTransition(order.getStatus(), newStatus, OrderStatus.SHIPPED,
          OrderStatus.CANCELED);
      case SHIPPED -> validateStatusTransition(order.getStatus(), newStatus, OrderStatus.DELIVERED);
      default -> throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
    }

    setOrderStatus(order, newStatus);
    return OrderDto.fromEntity(order);
  }

  /**
   * 배송지 수정
   *
   * @param orderId 주문 ID
   * @param request 배송지 수정 요청 DTO
   * @return 수정된 주문 DTO
   */
  @Transactional
  public OrderDto updateDeliveryAddress(Long orderId, OrderUpdateDto request, Long customerId) {
    Order order = findOrderById(orderId);

    validateCustomerAuthorization(customerId, order);

    validateOrderModifiable(order);
    order.setDeliveryAddress(request.getDeliveryAddress());

    return OrderDto.fromEntity(order);
  }

  // ================================= Helper methods ================================= //

  // 사용자 권한 확인 (어드민이 아니고, 고객 ID가 일치하지 않는 경우 예외)
  private void validateCustomerAuthorization(Long customerId, Order order) {
    if (!isAdmin() && !order.getCustomer().getId().equals(customerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
  }

  // 어드민 권한 확인
  private boolean isAdmin() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
  }

  // 장바구니 소유권 확인
  private Cart validateCartOwnership(Long customerId, Long cartId) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    if (!cart.getCustomer().getId().equals(customerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
    return cart;
  }

  // 장바구니 비어있는지 확인
  private void validateCartNotEmpty(Cart cart) {
    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }
  }

  // 주문 항목 생성, 재고량 반영
  private List<OrderItem> createOrderItemsFromCart(Cart cart) {
    return cart.getCartItems().stream()
        .map(cartItem -> {
          Product product = cartItem.getProduct();
          validateProductStatus(product);
          checkAndReduceStock(product, cartItem.getQuantity());
          return createOrderItemsFromCart(product, cartItem.getQuantity());
        })
        .toList();
  }

  // 장바구니 비우기
  private void clearCart(Cart cart) {
    cart.getCartItems().clear();
    cartRepository.save(cart);
  }

  // 주문 취소 가능 여부 확인 (주문이 대기중 상태가 아니면 취소 불가)
  private void validateOrderCancellable(Order order) {
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
    }
  }

  // 주문 수정 가능 여부 확인 (주문이 대기중 상태가 아니면 수정 불가)
  private void validateOrderModifiable(Order order) {
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
      throw new CustomException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
    }
  }

  // 주문 상태 검증
  private void validateStatusTransition(OrderStatus newStatus,
      OrderStatus... allowedStatuses) {
    if (!List.of(allowedStatuses).contains(newStatus)) {
      throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
    }
  }

  // 주문 상태 설정
  private void setOrderStatus(Order order, OrderStatus newStatus) {
    if (newStatus == OrderStatus.CANCELED) {
      restoreStock(order);
    }
    order.setStatus(newStatus);
  }

  // 상품 상태 확인 (상품 상태가 판매중이 아닌 경우 예외)
  private void validateProductStatus(Product product) {
    if (!product.getStatus().equals(ProductStatus.AVAILABLE)) {
      throw new CustomException(ErrorCode.PRODUCT_NOT_AVAILABLE);
    }
  }

  // 주문 취소 시 재고 복구
  private void restoreStock(Order order) {
    for (OrderItem orderItem : order.getOrderItems()) {
      Product product = orderItem.getProduct();
      Integer quantityToRestore = orderItem.getQuantity();

      // 재고 복구: 상품의 재고에 수량 더하기
      product.setStockQuantity(product.getStockQuantity() + quantityToRestore);
      productRepository.save(product);
    }
  }

  // 재고 확인 및 차감
  private synchronized void checkAndReduceStock(Product product, Integer quantity) {
    // 동시성을 위해 synchronized 사용
    if (product.getStockQuantity() < quantity) {
      throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
    }
    product.setStockQuantity(product.getStockQuantity() - quantity);
    productRepository.save(product);
  }

  // 주문 항목 생성
  private OrderItem createOrderItemsFromCart(Product product, Integer quantity) {
    return OrderItem.builder()
        .product(product)
        .quantity(quantity)
        .price(product.getPrice())
        .build();
  }

  // 주문 엔티티 생성
  private static Order buildOrder(OrderCreateDto.Request request, Cart cart,
      List<OrderItem> orderItems) {
    return Order.builder()
        .customer(cart.getCustomer())
        .status(OrderStatus.PENDING) // 기본값 대기중
        .deliveryAddress(request.getDeliveryAddress())
        .orderItems(orderItems)
        .cart(cart)
        .build();
  }

  // 주문 찾기
  private Order findOrderById(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
  }

  // 사용자 존재 여부 확인
  private void validateCustomerExists(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }
}
