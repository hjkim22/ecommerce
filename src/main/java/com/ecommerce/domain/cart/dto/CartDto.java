package com.ecommerce.domain.cart.dto;


import com.ecommerce.domain.cart.Cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

  private Long cartId;
  private Long customerId;
  private List<CartItemDto> cartItems;
  private BigDecimal totalPrice;

  public static CartDto fromEntity(Cart cartEntity) {
    return CartDto.builder()
        .cartId(cartEntity.getId())
        .customerId(cartEntity.getCustomer().getId())
        .cartItems(cartEntity.getCartItems().stream()
            .map(CartItemDto::fromEntity)
            .collect(Collectors.toList()))
        .totalPrice(cartEntity.getCartItems().stream()
            .map(item -> item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add))
        .build();
  }
}