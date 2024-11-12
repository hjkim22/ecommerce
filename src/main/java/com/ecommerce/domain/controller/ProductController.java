package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.dto.product.ProductUpdateDto;
import com.ecommerce.domain.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  private static final String ROLE_ACCESS_CONDITION =
      "hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')";

  @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
  @PostMapping
  public ResponseEntity<ProductCreateDto.Response> createProduct(
      @Valid @RequestBody ProductCreateDto.Request request,
      @JwtToken Long sellerId) {
    log.info("상품 생성 요청");
    ProductCreateDto.Response newProduct = productService.createProduct(request, sellerId);
    log.info("상품 생성 성공 - ID: {}", newProduct.getProductId());
    return ResponseEntity.status(CREATED).body(newProduct);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable("productId") Long id) {
    log.info("상품 정보 조회 요청 - ID: {}", id);
    ProductDto product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ProductDto>> searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long sellerId,
      @RequestParam(required = false) ProductStatus status,
      Pageable pageable) {
    log.info("상품 정보 조회 요청");

    Page<ProductDto> products = getSearchResults(name, sellerId, status, pageable);

    if (products.isEmpty()) {
      log.info("상품 검색 결과 없음");
      return ResponseEntity.noContent().build(); // 빈 리스트일 경우 204 No Content 반환
    }
    return ResponseEntity.ok(products);
  }

  @GetMapping("/findAll")
  public ResponseEntity<Page<ProductDto>> getProducts(Pageable pageable) {
    log.info("전체 상품 정보 조회 요청");
    Page<ProductDto> products = productService.getProducts(pageable);
    return ResponseEntity.ok(products);
  }

  @PreAuthorize(ROLE_ACCESS_CONDITION)
  @PutMapping("/{productId}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable("productId") Long id,
      @Valid @RequestBody ProductUpdateDto request,
      @JwtToken Long sellerId) {
    log.info("상품 업데이트 요청 - ID: {}", id);
    ProductDto updatedProduct = productService.updateProduct(id, request, sellerId);
    return ResponseEntity.ok(updatedProduct);
  }

  @PreAuthorize(ROLE_ACCESS_CONDITION)
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long id,
      @JwtToken Long sellerId) {
    log.info("상품 삭제 요청 - ID: {}", id);
    productService.deleteProduct(id, sellerId);
    return ResponseEntity.noContent().build();
  }

  // 상품 검색 로직을 처리
  private Page<ProductDto> getSearchResults(String name, Long sellerId, ProductStatus status, Pageable pageable) {
    if (name != null && !name.isEmpty()) {
      return productService.getProductByName(name, pageable);
    }
    if (sellerId != null) {
      return productService.getProductBySellerId(sellerId, pageable);
    }
    if (status != null) {
      return productService.getProductByStatus(status, pageable);
    }
    return Page.empty(); // 조건 없으면 빈 페이지 반환
  }
}
