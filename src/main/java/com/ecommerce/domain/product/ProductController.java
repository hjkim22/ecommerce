package com.ecommerce.domain.product;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.security.JwtToken;
import com.ecommerce.domain.product.dto.ProductCreateDto;
import com.ecommerce.domain.product.dto.ProductDto;
import com.ecommerce.domain.product.dto.ProductUpdateDto;
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

  // 상품 생성
  @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
  @PostMapping
  public ResponseEntity<ProductCreateDto.Response> createProduct(
      @Valid @RequestBody ProductCreateDto.Request productCreateRequest,
      @JwtToken Long sellerId) {
    log.info("상품 생성 요청");
    ProductCreateDto.Response newProduct = productService.createProduct(productCreateRequest,
        sellerId);
    log.info("상품 생성 성공 - ID: {}", newProduct.getProductId());
    return ResponseEntity.status(CREATED).body(newProduct);
  }

  // 판매자 ID로 상품 조회
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable("productId") Long productId) {
    log.info("상품 정보 조회 요청 - ID: {}", productId);
    ProductDto product = productService.getProductById(productId);
    return ResponseEntity.ok(product);
  }

  // 상품 검색
  @GetMapping("/search")
  public ResponseEntity<Page<ProductDto>> searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long sellerId,
      @RequestParam(required = false) ProductStatus productStatus,
      Pageable pageable) {
    log.info("상품 정보 조회 요청");

    Page<ProductDto> products = getSearchResults(name, sellerId, productStatus, pageable);

    if (products.isEmpty()) {
      log.info("상품 검색 결과 없음");
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(products);
  }

  // 전체 상품 조회
  @GetMapping
  public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
    log.info("전체 상품 정보 조회 요청");
    Page<ProductDto> products = productService.getAllProducts(pageable);
    return ResponseEntity.ok(products);
  }

  // 상품 업데이트
  @PreAuthorize(ROLE_ACCESS_CONDITION)
  @PutMapping("/{productId}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable("productId") Long productId,
      @Valid @RequestBody ProductUpdateDto productUpdateRequest,
      @JwtToken Long sellerId) {
    log.info("상품 업데이트 요청 - ID: {}", productId);
    ProductDto updatedProduct = productService.updateProduct(productId, productUpdateRequest,
        sellerId);
    return ResponseEntity.ok(updatedProduct);
  }

  // 상품 삭제
  @PreAuthorize(ROLE_ACCESS_CONDITION)
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId,
      @JwtToken Long sellerId) {
    log.info("상품 삭제 요청 - ID: {}", productId);
    productService.deleteProduct(productId, sellerId);
    return ResponseEntity.noContent().build();
  }

  // 상품 검색 로직 처리
  private Page<ProductDto> getSearchResults(String name, Long sellerId,
      ProductStatus productStatus, Pageable pageable) {
    if ((name == null || name.isEmpty()) && sellerId == null && productStatus == null) {
      return productService.getAllProducts(pageable);
    }
    if (name != null && !name.isEmpty()) {
      return productService.getProductByName(name, pageable);
    }
    if (sellerId != null) {
      return productService.getProductBySellerId(sellerId, pageable);
    }
    return productService.getProductByStatus(productStatus, pageable);
  }
}
