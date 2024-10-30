package com.ecommerce.domain.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping("/create")
  public ResponseEntity<ProductCreateDto.Response> createProduct(
      @Valid @RequestBody ProductCreateDto.Request request,
      @RequestParam Long sellerId,
      HttpServletRequest httpServletRequest) {

    ProductCreateDto.Response newProduct = productService.createProduct(request, sellerId,
        httpServletRequest);
    return ResponseEntity.status(CREATED).body(newProduct);
  }
}
