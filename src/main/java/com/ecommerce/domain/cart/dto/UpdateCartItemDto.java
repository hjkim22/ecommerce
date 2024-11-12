package com.ecommerce.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class UpdateCartItemDto {

  @NotNull(message = "상품 수량은 필수입니다.")
  @Min(value = 1, message = "상품 수량은 1 이상이어야 합니다.")
  private Integer quantity;
}
