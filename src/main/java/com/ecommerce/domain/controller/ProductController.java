package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.dto.product.ProductUpdateDto;
import com.ecommerce.domain.service.ProductService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PostMapping
  public ResponseEntity<ProductCreateDto.Response> createProduct(
      @Valid @RequestBody ProductCreateDto.Request request,
      @JwtToken Long sellerId) {
    log.info("상품 생성 요청");
    ProductCreateDto.Response newProduct = productService.createProduct(request, sellerId);
    log.info("상품 생성 성공 - ID: {}", newProduct.getProductId());
    return ResponseEntity.status(CREATED).body(newProduct);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable("productId") Long id) {
    log.info("상품 정보 조회 요청 - ID: {}", id);
    ProductDto product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/search")
  public ResponseEntity<List<ProductDto>> searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long sellerId,
      @RequestParam(required = false) ProductStatus status) {
    log.info("상품 검색 요청");
    List<ProductDto> products = new ArrayList<>();

    if (name != null && !name.isEmpty()) {
      products.addAll(productService.getProductByName(name));
    }
    if (sellerId != null) {
      products.addAll(productService.getProductBySellerId(sellerId));
    }
    if (status != null) {
      products.addAll(productService.getProductByStatus(status));
    }
    if (products.isEmpty()) {
      log.info("상품 검색 결과 없음");
      return ResponseEntity.noContent().build(); // 빈 리스트일 경우 204 No Content 반환
    }

    return ResponseEntity.ok(products);
  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PutMapping("/{productId}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable("productId") Long id,
      @Valid @RequestBody ProductUpdateDto request,
      @JwtToken Long sellerId) {
    log.info("상품 업데이트 요청 - ID: {}", id);
    ProductDto updatedProduct = productService.updateProduct(id, request, sellerId);
    return ResponseEntity.ok(updatedProduct);
  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long id,
      @JwtToken Long sellerId) {
    log.info("상품 삭제 요청 - ID: {}", id);
    productService.deleteProduct(id, sellerId);
    return ResponseEntity.noContent().build();
  }
}
