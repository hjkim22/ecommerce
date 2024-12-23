package com.ecommerce.domain.order;

import com.ecommerce.common.entity.BaseTimeEntity;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.domain.cart.Cart;
import com.ecommerce.domain.member.Member;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member customer;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private String deliveryAddress;
  private BigDecimal totalPrice;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY) // 1개의 장바구니와 1:1
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  public void addOrderItems(List<OrderItem> orderItems) {
    this.orderItems = new ArrayList<>(orderItems);
    BigDecimal calculatedTotalPrice = BigDecimal.ZERO;
    // 총 가격 계산
    for (OrderItem orderItem : orderItems) {
      orderItem.setOrder(this);
      calculatedTotalPrice = calculatedTotalPrice.add(
          orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
    }
    this.totalPrice = calculatedTotalPrice;
  }
}
