package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.security.TokenProvider;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductCreateDto.Request;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;
  private final TokenProvider tokenProvider;

  // 상품 등록
  public ProductCreateDto.Response createProduct(ProductCreateDto.Request request,
      HttpServletRequest httpServletRequest) {
    String token = tokenProvider.extractToken(httpServletRequest);
    Long userId = validateTokenAndExtractUserId(token);
    MemberEntity seller = validateSeller(request.getSellerId(), userId);
    ProductEntity product = createProductEntityFromDto(request, seller);

    return new ProductCreateDto.Response(product.getId(), product.getProductName(), "상품 등록 완료");
  }

  // 상품 조회(id)
  public ProductDto getProductById(Long id) {
    ProductEntity product = productRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    return ProductDto.fromEntity(product);
  }

  // 상품 조회(productName)
  public List<ProductDto> getProductByName(String name) {
    List<ProductEntity> products = productRepository.findByProductNameContaining(name);

    if (products.isEmpty()) {
      throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    return products.stream()
        .map(ProductDto::fromEntity)
        .toList();
  }

  // 상품 조회(sellerId)
  public List<ProductDto> getProductBySellerId(Long id) {
    if (!memberRepository.existsById(id)) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    List<ProductEntity> products = productRepository.findBySellerId(id);

    if (products.isEmpty()) {
      throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    return products.stream()
        .map(ProductDto::fromEntity)
        .toList();
  }

  // 상품 상태별 조회
  public List<ProductDto> getProductByStatus(ProductStatus status) {
    List<ProductEntity> products = productRepository.findByStatus(status);
    if (products.isEmpty()) {
      throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    return products.stream()
        .map(ProductDto::fromEntity)
        .toList();
  }

  // 상품 업데이트
  // 상품 삭제

  // 토큰 검증, id 추출
  private Long validateTokenAndExtractUserId(String token) {
    if (token == null || !tokenProvider.isValidToken(token)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    return tokenProvider.extractUserIdFromToken(token);
  }

  // seller 검증
  private MemberEntity validateSeller(Long sellerId, Long userId) {
    MemberEntity seller = memberRepository.findById(sellerId)
        .orElseThrow(() -> new CustomException(ErrorCode.SELLER_NOT_FOUND));

    if (!sellerId.equals(userId)) {
      throw new CustomException(ErrorCode.INVALID_SELLER_ACCESS);
    }

    return seller;
  }

  // 상품 엔티티 생성
  private ProductEntity createProductEntityFromDto(Request request, MemberEntity seller) {
    return productRepository.save(ProductEntity.builder()
        .productName(request.getProductName())
        .description(request.getDescription())
        .price(request.getPrice())
        .stockQuantity(request.getStockQuantity())
        .status(request.getStatus()) // 기본값 AVAILABLE
        .seller(seller)
        .build()
    );
  }
}
