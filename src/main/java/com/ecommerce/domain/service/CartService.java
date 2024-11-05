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

  // 장바구니 생성
  public void createCartForMember(MemberEntity member) {
    CartEntity cart = CartEntity.builder()
        .customer(member)
        .build();
    cartRepository.save(cart);
  }

  // 장바구니에 상품 담기
  @Transactional
  public AddToCartDto.Response addItemToCart(Long customerId, AddToCartDto.Request request) {
    CartEntity cart = findCartByCustomerId(customerId);
    ProductEntity product = findProductById(request.getProductId());

    validateProductStatus(product);
    validateProductQuantity(product, request.getQuantity());

    CartItemEntity existingCartItem = findCartItem(cart, product);

    if (existingCartItem != null) {
      int updatedQuantity = existingCartItem.getQuantity() + request.getQuantity();
      validateProductQuantity(product, updatedQuantity);
      existingCartItem.setQuantity(updatedQuantity);
    } else {
      CartItemEntity newCartItem = createCartItem(cart, product, request.getQuantity());
      cart.getCartItems().add(newCartItem);
    }
    cartRepository.save(cart);

    return new AddToCartDto.Response(product.getId(), request.getQuantity(), "장바구니에 상품을 담았습니다.");
  }

  // cartId - 조회
  public CartDto getCartById(Long cartId) {
    CartEntity cart = findCartById(cartId);

    return CartDto.fromEntity(cart);
  }

  // customerId - 조회
  public CartDto getCartByCustomerId(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    CartEntity cart = findCartByCustomerId(customerId);

    return CartDto.fromEntity(cart);
  }

  // 수량 수정
  @Transactional
  public AddToCartDto.Response updateCartItemQuantity(Long cartId, Long productId,
      UpdateCartItemDto request) {
    CartEntity cart = findCartById(cartId);
    ProductEntity product = findProductById(productId);

    validateProductQuantity(product, request.getQuantity());

    CartItemEntity existingCartItem = findCartItem(cart, product);
    existingCartItem.setQuantity(request.getQuantity());

    return new AddToCartDto.Response(productId, request.getQuantity(), "수량 수정 완료");
  }

  // 비우기
  public void clearCart(Long cartId) {
    CartEntity cart = findCartById(cartId);

    if (cart.getCartItems().isEmpty()) {
      throw new CustomException(ErrorCode.CART_EMPTY);
    }

    cart.getCartItems().clear();
    cartRepository.save(cart);
  }

  // CartItemEntity 생성
  private CartItemEntity createCartItem(CartEntity cart, ProductEntity product, Integer quantity) {
    return CartItemEntity.builder()
        .cart(cart)
        .product(product)
        .quantity(quantity)
        .build();
  }

  private CartEntity findCartById(Long cartId) {
    return cartRepository.findById(cartId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
  }

  private CartEntity findCartByCustomerId(Long customerId) {
    return cartRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
  }

  private ProductEntity findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  private void validateProductStatus(ProductEntity product) {
    if (product.getStatus() == ProductStatus.INACTIVE) {
      throw new CustomException(ErrorCode.PRODUCT_INACTIVE);
    }
    if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
      throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }
  }

  private void validateProductQuantity(ProductEntity product, int quantity) {
    if (quantity > product.getStockQuantity()) {
      throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
    }
  }

  private CartItemEntity findCartItem(CartEntity cart, ProductEntity product) {
    return cart.getCartItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);
  }
}
