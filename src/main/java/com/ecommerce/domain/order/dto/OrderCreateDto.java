package com.ecommerce.domain.order.dto;

import com.ecommerce.common.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OrderCreateDto {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotNull(message = "장바구니 ID는 필수입니다.")
    private Long cartId;
    @NotBlank(message = "배송지는 필수입니다.")
    private String deliveryAddress;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {

    private Long cartId;
    private OrderStatus status;
    private String message;
  }
}
