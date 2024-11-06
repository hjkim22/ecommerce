package com.ecommerce.domain.repository;

import com.ecommerce.domain.entity.CartEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

  Optional<CartEntity> findByCustomerId(Long customerId);
}
