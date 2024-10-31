package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.dto.product.ProductUpdateDto;
import com.ecommerce.domain.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PostMapping("/create")
  public ResponseEntity<ProductCreateDto.Response> createProduct(
      @Valid @RequestBody ProductCreateDto.Request request,
      HttpServletRequest httpServletRequest) {

    ProductCreateDto.Response newProduct = productService.createProduct(request,
        httpServletRequest);
    return ResponseEntity.status(CREATED).body(newProduct);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable("productId") Long id) {
    ProductDto product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<List<ProductDto>> getProductByName(@PathVariable("name") String name) {
    List<ProductDto> products = productService.getProductByName(name);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/seller-id/{sellerId}")
  public ResponseEntity<List<ProductDto>> getProductBySellerId(@PathVariable("sellerId") Long id) {
    List<ProductDto> products = productService.getProductBySellerId(id);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<ProductDto>> getProductByStatus(
      @PathVariable("status") ProductStatus status) {

    List<ProductDto> products = productService.getProductByStatus(status);
    return ResponseEntity.ok(products);
  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PutMapping("/{productId}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable("productId") Long id,
      @Valid @RequestBody ProductUpdateDto request,
      HttpServletRequest httpServletRequest) {

    ProductDto updatedProduct = productService.updateProduct(id, request, httpServletRequest);
    return ResponseEntity.ok(updatedProduct);
  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long id,
      HttpServletRequest httpServletRequest) {
    productService.deleteProduct(id, httpServletRequest);
    return ResponseEntity.noContent().build();
  }
}
