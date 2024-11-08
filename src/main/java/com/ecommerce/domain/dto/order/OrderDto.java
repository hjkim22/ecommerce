package com.ecommerce.domain.dto.order;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.domain.dto.orderItem.OrderItemDto;
import com.ecommerce.domain.entity.OrderEntity;
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
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

  private Long orderId;
  private Long customerId;
  private OrderStatus status;
  private String deliveryAddress;
  private BigDecimal totalPrice;
  private List<OrderItemDto> orderItems;

  public static OrderDto fromEntity(OrderEntity orderEntity) {
    return OrderDto.builder()
        .orderId(orderEntity.getId())
        .customerId(orderEntity.getCustomer().getId())
        .status(orderEntity.getStatus())
        .deliveryAddress(orderEntity.getDeliveryAddress())
        .totalPrice(orderEntity.getTotalPrice())
        .orderItems(orderEntity.getOrderItems().stream()
            .map(OrderItemDto::fromEntity)
            .toList())
        .build();
  }
}
