package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.security.TokenProvider;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductCreateDto.Request;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
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
  // 상품 조회(productName)
  // 상품 조회(sellerId)
  // 상품 조회(sellerName)
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
