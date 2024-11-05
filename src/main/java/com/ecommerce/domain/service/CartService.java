package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.cart.AddToCartDto;
import com.ecommerce.domain.dto.cart.CartDto;
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
    CartEntity cart = cartRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    ProductEntity product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    // 상품 비활성화 상태
    if (product.getStatus() == ProductStatus.INACTIVE) {
      throw new CustomException(ErrorCode.PRODUCT_INACTIVE);
    }
    // 상품 품절 상태
    if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
      throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }

    CartItemEntity existingCartItem = cart.getCartItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);

    if (existingCartItem != null) {
      int updatedQuantity = existingCartItem.getQuantity() + request.getQuantity();

      if (updatedQuantity > product.getStockQuantity()) {
        throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
      }

      existingCartItem.setQuantity(updatedQuantity);
    } else {
      if (request.getQuantity() > product.getStockQuantity()) {
        throw new CustomException(ErrorCode.QUANTITY_EXCEEDS_STOCK);
      }

      CartItemEntity newCartItem = createCartItem(cart, product, request.getQuantity());
      cart.getCartItems().add(newCartItem);
    }
    cartRepository.save(cart);

    return new AddToCartDto.Response(product.getId(), request.getQuantity(), "장바구니에 상품을 담았습니다.");
  }

  // cartId - 조회
  public CartDto getCartById(Long cartId) {
    CartEntity cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    return CartDto.fromEntity(cart);
  }

  // customerId - 조회
  public CartDto getCartByCustomerId(Long customerId) {
    memberRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    CartEntity cart = cartRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

    return CartDto.fromEntity(cart);
  }

  // CartItemEntity 생성
  private CartItemEntity createCartItem(CartEntity cart, ProductEntity product, Integer quantity) {
    return CartItemEntity.builder()
        .cart(cart)
        .product(product)
        .quantity(quantity)
        .build();
  }
}
