package com.ecommerce.domain.order;

import com.ecommerce.common.entity.BaseTimeEntity;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.domain.cart.CartEntity;
import com.ecommerce.domain.member.MemberEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity customer;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private String deliveryAddress; // 배송지
  private BigDecimal totalPrice;  // 총 가격

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemEntity> orderItems = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY) // 1개의 장바구니와 1:1
  @JoinColumn(name = "cart_id", nullable = false)
  private CartEntity cart;

  public void addOrderItems(List<OrderItemEntity> orderItems) {
    this.orderItems = new ArrayList<>(orderItems);
    BigDecimal calculatedTotalPrice = BigDecimal.ZERO;
    // 총 가격 계산
    for (OrderItemEntity orderItem : orderItems) {
      orderItem.setOrder(this);
      calculatedTotalPrice = calculatedTotalPrice.add(
          orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
    }
    this.totalPrice = calculatedTotalPrice;
  }
}
