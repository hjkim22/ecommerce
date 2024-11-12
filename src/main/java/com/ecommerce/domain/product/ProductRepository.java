package com.ecommerce.domain.product;

import com.ecommerce.common.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  Page<ProductEntity> findByProductNameContaining(String name, Pageable pageable);

  Page<ProductEntity> findBySellerId(Long sellerId, Pageable pageable);

  Page<ProductEntity> findByStatus(ProductStatus status, Pageable pageable);
}