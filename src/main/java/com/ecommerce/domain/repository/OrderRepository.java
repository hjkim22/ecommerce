package com.ecommerce.domain.repository;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.domain.entity.OrderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  List<OrderEntity> findByCustomerId(Long customerId);
  List<OrderEntity> findByStatus(OrderStatus status);
}
