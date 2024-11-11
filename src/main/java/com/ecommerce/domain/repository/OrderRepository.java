package com.ecommerce.domain.repository;

import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.domain.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);
  Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
}
