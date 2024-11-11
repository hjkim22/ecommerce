package com.ecommerce.domain.controller;

import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.cart.AddToCartDto;
import com.ecommerce.domain.dto.cart.CartDto;
import com.ecommerce.domain.dto.cartItem.UpdateCartItemDto;
import com.ecommerce.domain.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/cart")
public class CartController {

  private final CartService cartService;

  // 담기
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PostMapping
  public ResponseEntity<AddToCartDto.Response> addItemToCart(
      @Valid @RequestBody AddToCartDto.Request request,
      @JwtToken Long customerId) {
    log.info("장바구니 상품 추가 요청");
    AddToCartDto.Response response = cartService.addItemToCart(customerId, request);
    log.info("장바구니 상품 추가 완료 - 상품 ID: {}", response.getProductId());
    return ResponseEntity.ok(response);
  }

  // cartId 조회
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/{cartId}")
  public ResponseEntity<CartDto> getCartById(@PathVariable("cartId") Long id) {
    log.info("장바구니 조회 요청 - 장바구니 ID: {}", id);
    CartDto cart = cartService.getCartById(id);
    return ResponseEntity.ok(cart);
  }

  // customerId 조회
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/customerId/{customerId}")
  public ResponseEntity<CartDto> getCartByCustomerId(@PathVariable("customerId") Long id) {
    log.info("장바구니 조회 요청 - 사용자 ID: {}", id);
    CartDto cart = cartService.getCartByCustomerId(id);
    return ResponseEntity.ok(cart);
  }

  // 수정
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PatchMapping("/{cartId}/items/{productId}")
  public ResponseEntity<AddToCartDto.Response> updateCartItemQuantity(
      @PathVariable Long cartId,
      @PathVariable Long productId,
      @Valid @RequestBody UpdateCartItemDto request) {
    log.info("장바구니 상품 수량 수정 요청 - 장바구니 ID: {}, 상품 ID: {}, 수량: {}", cartId, productId,
        request.getQuantity());
    AddToCartDto.Response response = cartService.updateCartItemQuantity(cartId, productId, request);
    return ResponseEntity.ok(response);
  }

  // 비우기
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> clearCart(@PathVariable("cartId") Long cartId) {
    log.info("장바구니 비우기 요청 - 장바구니 ID: {}", cartId);
    cartService.clearCart(cartId);
    return ResponseEntity.noContent().build();
  }
}
