package com.ecommerce.domain.cart;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.cart.dto.AddToCartDto;
import com.ecommerce.domain.cart.dto.CartDto;
import com.ecommerce.domain.cart.dto.UpdateCartItemDto;
import com.ecommerce.domain.member.MemberEntity;
import com.ecommerce.domain.member.MemberRepository;
import com.ecommerce.domain.product.ProductEntity;
import com.ecommerce.domain.product.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  /**
   * 회원 가입 시 장바구니 생성
   *
   * @param member 생성 회원 정보
   */
  public void createCartForMember(MemberEntity member) {
    CartEntity cart = CartEntity.builder()
        .customer(member)
        .build();
    cartRepository.save(cart);
  }

  /**
   * 장바구니에 상품 추가. 이미 상품이 존재 시 수량 증가
   *
   * @param customerId 사용자 ID
   * @param request    추가할 상품 정보
   * @return 상품 ID, 수량, 메시지
   */
  @Transactional
  public AddToCartDto.Response addProductToCart(Long customerId, AddToCartDto.Request request) {
    CartEntity cart = findCartByCustomerId(customerId);
    ProductEntity product = findProductById(request.getProductId());

    validateProductStatus(product);
    validateProductQuantity(product, request.getQuantity());

    Optional<CartItemEntity> existingCartItem = findCartItemByProduct(cart, product);

    if (existingCartItem.isPresent()) {
      CartItemEntity item = existingCartItem.get();
      int updatedQuantity = item.getQuantity() + request.getQuantity();
      validateProductQuantity(product, updatedQuantity);
      item.setQuantity(updatedQuantity);
    } else {
      CartItemEntity newCartItem = createCartItem(cart, product, request.getQuantity());
      cart.getCartItems().add(newCartItem);
    }

    cartRepository.save(cart);
    return new AddToCartDto.Response(product.getId(), request.getQuantity(), "상품 추가 완료");
  }

  /**
   * 장바구니 정보 조회
   *
   * @param cartId 장바구니 ID
   * @return 장바구니 정보 DTO
   */
  public CartDto getCartById(Long cartId) {
    CartEntity cart = findCartById(cartId);
    return CartDto.fromEntity(cart);
  }

  /**
   * 장바구니 정보 조회
   *
   * @param customerId 사용자 ID
   * @return 장바구니 정보 DTO
   */
  public CartDto getCartByCustomerId(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    CartEntity cart = findCartByCustomerId(customerId);
    return CartDto.fromEntity(cart);
  }

  /**
   * 장바구니 상품 수량 업데이트
   *
   * @param cartId    장바구니 ID
   * @param productId 상품 ID
   * @param request   수량 수정 정보 DTO
   * @return 상품 ID, 수량, 메시지
   */
  @Transactional
  public AddToCartDto.Response updateCartItemQuantity(Long cartId, Long productId,
      UpdateCartItemDto request) {
    CartEntity cart = findCartById(cartId);
    ProductEntity product = findProductById(productId);

    validateProductQuantity(product, request.getQuantity());

    CartItemEntity existingCartItem = findCartItemByProduct(cart, product)
        .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
    existingCartItem.setQuantity(request.getQuantity());

    return new AddToCartDto.Response(productId, request.getQuantity(), "수량 수정 완료");
  }

  /**
   * 장바구니 특정 상품 삭제
   *
   * @param cartId    장바구니 ID
   * @param productId 상품 ID
   */
  public void removeProductFromCart(Long cartId, Long productId, Long customerId) {
    CartEntity cart = findCartById(cartId);
    ProductEntity product = findProductById(productId);

    validateCustomerAuthorization(customerId, cart);
    CartItemEntity cartItem = findCartItemByProduct(cart, product)
        .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

    if (cartItem == null) {
      throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
    }

    cart.getCartItems().remove(cartItem);
    cartRepository.save(cart);
  }

  /**
   * 장바구니 비우기
   *
   * @param cartId 장바구니 ID
   */
  public void clearCart(Long cartId, Long customerId) {
    CartEntity cart = findCartById(cartId);
    validateCustomerAuthorization(customerId, cart);

    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }

    cart.getCartItems().clear();
    cartRepository.save(cart);
  }

  // ================================= Helper methods ================================= //

  // 사용자 권한 확인 (어드민이 아니고, 고객 ID가 일치하지 않는 경우 예외)
  private void validateCustomerAuthorization(Long customerId, CartEntity cart) {
    if (!cart.getCustomer().getId().equals(customerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
  }

  // 장바구니 엔티티 생성
  private CartItemEntity createCartItem(CartEntity cart, ProductEntity product, Integer quantity) {
    return CartItemEntity.builder()
        .cart(cart)
        .product(product)
        .quantity(quantity)
        .build();
  }

  // 장바구니 조회(cartId), 없을 경우 예외
  private CartEntity findCartById(Long cartId) {
    return cartRepository.findById(cartId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
  }

  // 장바구니 조회(customerId), 없을 경우 예외
  private CartEntity findCartByCustomerId(Long customerId) {
    return cartRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
  }

  // 상품 조회(productId), 없을 경우 예외
  private ProductEntity findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  // 상품 비활성, 품절 상태 시 예외
  private void validateProductStatus(ProductEntity product) {
    if (product.getStatus() == ProductStatus.INACTIVE) {
      throw new CustomException(ErrorCode.PRODUCT_INACTIVE);
    }
    if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
      throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }
  }

  // 요청 수량이 상품 재고량 초과 시 예외
  private void validateProductQuantity(ProductEntity product, int quantity) {
    if (quantity > product.getStockQuantity()) {
      throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
    }
  }

  // 장바구니에서 해당 아이템 조회, 없을 경우 null
  private Optional<CartItemEntity> findCartItemByProduct(CartEntity cart, ProductEntity product) {
    return cart.getCartItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst();
  }
}
