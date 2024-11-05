package com.ecommerce.domain.controller;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.security.TokenProvider;
import com.ecommerce.domain.dto.cart.AddToCartDto;
import com.ecommerce.domain.dto.cart.CartDto;
import com.ecommerce.domain.dto.cartItem.UpdateCartItemDto;
import com.ecommerce.domain.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
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
  private final TokenProvider tokenProvider;

  // HTTP 요청에서 사용자 ID 추출
  private Long extractCustomerId(HttpServletRequest httpServletRequest) {
    String token = tokenProvider.extractToken(httpServletRequest);
    if (token == null || !tokenProvider.isValidToken(token)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    return tokenProvider.extractUserIdFromToken(token); // 토큰에서 사용자 ID 추출
  }

  // 담기
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PostMapping
  public ResponseEntity<AddToCartDto.Response> addItemToCart(
      @Valid @RequestBody AddToCartDto.Request request,
      HttpServletRequest httpServletRequest) {
    Long customerId = extractCustomerId(httpServletRequest);
    AddToCartDto.Response response = cartService.addItemToCart(customerId, request);
    return ResponseEntity.ok(response);
  }

  // cartId 조회
  @GetMapping("/{cartId}")
  public ResponseEntity<CartDto> getCartById(@PathVariable("cartId") Long id) {
    CartDto cart = cartService.getCartById(id);
    return ResponseEntity.ok(cart);
  }

  // customerId 조회
  @GetMapping("/customerId/{customerId}")
  public ResponseEntity<CartDto> getCartByCustomerId(@PathVariable("customerId") Long id) {
    CartDto cart = cartService.getCartByCustomerId(id);
    return ResponseEntity.ok(cart);
  }

  // 수정
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PatchMapping("/{cartId}/items/{productId}")
  public ResponseEntity<AddToCartDto.Response> updateCartItemQuantity(
      @PathVariable Long cartId,
      @PathVariable Long productId,
      @Valid @RequestBody UpdateCartItemDto request,
      HttpServletRequest httpServletRequest) {
    extractCustomerId(httpServletRequest);
    AddToCartDto.Response response = cartService.updateCartItemQuantity(cartId, productId, request);
    return ResponseEntity.ok(response);
  }

  // 비우기
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> clearCart(@PathVariable("cartId") Long cartId) {
    cartService.clearCart(cartId);
    return ResponseEntity.noContent().build();
  }
}
