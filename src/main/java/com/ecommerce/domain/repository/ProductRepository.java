package com.ecommerce.domain.repository;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.domain.entity.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  List<ProductEntity> findByProductNameContaining(String name);
  List<ProductEntity> findBySellerId(Long sellerId);
  List<ProductEntity> findByStatus(ProductStatus status);
}