package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.cart.AddToCartDto;
import com.ecommerce.domain.dto.cart.CartDto;
import com.ecommerce.domain.dto.cartItem.UpdateCartItemDto;
import com.ecommerce.domain.entity.CartEntity;
import com.ecommerce.domain.entity.CartItemEntity;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.ProductRepository;
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
   * @param member 생성 회원
   */
  public void createCartForMember(MemberEntity member) {
    CartEntity cart = CartEntity.builder()
        .customer(member)
        .build();
    cartRepository.save(cart);
  }

  /**
   * 장바구니에 상품 추가. 이미 상품이 있을 경우 수량 증가
   * @param customerId 사용자 ID
   * @param request 추가할 상품 정보
   * @return 메시지, 수량
   */
  @Transactional
  public AddToCartDto.Response addProductToCart(Long customerId, AddToCartDto.Request request) {
    CartEntity cart = findCartByCustomerId(customerId);
    ProductEntity product = findProductById(request.getProductId());

    validateProductStatus(product);
    validateProductQuantity(product, request.getQuantity());

    CartItemEntity existingCartItem = findCartItemByProduct(cart, product);

    if (existingCartItem != null) {
      int updatedQuantity = existingCartItem.getQuantity() + request.getQuantity();
      validateProductQuantity(product, updatedQuantity);
      existingCartItem.setQuantity(updatedQuantity);
    } else {
      CartItemEntity newCartItem = createCartItem(cart, product, request.getQuantity());
      cart.getCartItems().add(newCartItem);
    }
    cartRepository.save(cart);

    return new AddToCartDto.Response(product.getId(), request.getQuantity(), "상품 추가 완료");
  }

  /**
   * 장바구니 정보 조회
   * @param cartId 장바구니 ID
   * @return 장바구니 정보 DTO
   */
  public CartDto getCartById(Long cartId) {
    CartEntity cart = findCartById(cartId);

    return CartDto.fromEntity(cart);
  }

  /**
   * 장바구니 정보 조회
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
   * 장바구니 수량 업데이트
   * @param cartId 장바구니 ID
   * @param productId 상품 ID
   * @param request 수량 수정 정보 DTO
   * @return 메시지, 수정된 수량
   */
  @Transactional
  public AddToCartDto.Response updateCartItemQuantity(Long cartId, Long productId,
      UpdateCartItemDto request) {
    CartEntity cart = findCartById(cartId);
    ProductEntity product = findProductById(productId);

    validateProductQuantity(product, request.getQuantity());

    CartItemEntity existingCartItem = findCartItemByProduct(cart, product);
    existingCartItem.setQuantity(request.getQuantity());

    return new AddToCartDto.Response(productId, request.getQuantity(), "수량 수정 완료");
  }

  // 특정 상품 삭제
  public void removeProductFromCart(Long cartId, Long productId) {
    CartEntity cart = findCartById(cartId);
    ProductEntity product = findProductById(productId);

    CartItemEntity cartItem = findCartItemByProduct(cart, product);

    if (cartItem == null) {
      throw new CustomException(ErrorCode.ITEM_NOT_FOUND);
    }

    cart.getCartItems().remove(cartItem);
    cartRepository.save(cart);
  }

  /**
   * 장바구니 비우기
   * @param cartId 장바구니 ID
   */
  public void clearCart(Long cartId) {
    CartEntity cart = findCartById(cartId);

    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }

    cart.getCartItems().clear();
    cartRepository.save(cart);
  }

  /**
   * 장바구니 엔티티 생성
   * @param cart 장바구니 엔티티
   * @param product 상품 엔티티
   * @param quantity 수량
   * @return 생성된 장바구니 아이템 엔티티
   */
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

  // 상품 조회, 없을 경우 예외
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
  private CartItemEntity findCartItemByProduct(CartEntity cart, ProductEntity product) {
    return cart.getCartItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);
  }
}
