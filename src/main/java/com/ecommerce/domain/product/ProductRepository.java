package com.ecommerce.domain.product;

import com.ecommerce.common.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findByProductNameContaining(String name, Pageable pageable);

  Page<Product> findBySellerId(Long sellerId, Pageable pageable);

  Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}