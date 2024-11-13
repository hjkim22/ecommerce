package com.ecommerce.domain.order;

import com.ecommerce.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Page<Order> findByCustomerId(Long customerId, Pageable pageable);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
