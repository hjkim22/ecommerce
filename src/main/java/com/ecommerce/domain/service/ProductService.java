package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductCreateDto.Request;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.dto.product.ProductUpdateDto;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  /**
   * 상품 등록
   * @param request 상품 생성 요청 데이터
   * @param sellerId 판매자 ID
   * @return 상품 등록 결과
   */
  public ProductCreateDto.Response createProduct(ProductCreateDto.Request request, Long sellerId) {
    MemberEntity seller = validateSeller(request.getSellerId(), sellerId); // 판매자 유효성 검사
    ProductEntity product = buildProductEntity(request, seller); // DTO 로부터 상품 엔티티 생성

    return new ProductCreateDto.Response(product.getId(), "상품 등록 완료");
  }

  /**
   * 상품 정보 조회
   * @param id 상품 ID
   * @return 상품 DTO
   */
  public ProductDto getProductById(Long id) {
    ProductEntity product = productRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    return ProductDto.fromEntity(product);
  }

  /**
   * 상품 정보 조회
   * @param name 상품명
   * @return 상품 DTO 리스트
   */
  public List<ProductDto> getProductByName(String name) {
    List<ProductEntity> products = productRepository.findByProductNameContaining(name);

    return products.stream()
        .map(ProductDto::fromEntity)
        .toList(); // 조회된 상품이 없더라도 빈 리스트를 반환
  }

  /**
   * 상품 정보 조회
   * @param id 판매자 ID
   * @return 상품 DTO 리스트
   */
  public List<ProductDto> getProductBySellerId(Long id) {
    memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    List<ProductEntity> products = productRepository.findBySellerId(id);

    if (products.isEmpty()) {
      throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    return products.stream()
        .map(ProductDto::fromEntity)
        .toList();
  }

  /**
   * 상품 정보 조회
   * @param status 상품 상태
   * @return 상품 DTO 리스트
   */
  public List<ProductDto> getProductByStatus(ProductStatus status) {
    return productRepository.findByStatus(status).stream()
        .map(ProductDto::fromEntity)
        .toList();
  }

  /**
   * 상품 정보 업데이트
   * @param id 상품 ID
   * @param request 업데이트 요청 데이터
   * @param sellerId 판매자 ID
   * @return 업데이트된 상품 DTO
   */
  @Transactional
  public ProductDto updateProduct(Long id, ProductUpdateDto request, Long sellerId) {
    ProductEntity product = validateProductAndAccess(id, sellerId); // 상품 및 접근 유효성 검사

    // 요청 데이터에 따라 상품 정보 업데이트
    if (request.getProductName() != null) {
      product.setProductName(request.getProductName());
    }
    if (request.getDescription() != null) {
      product.setDescription(request.getDescription());
    }
    if (request.getPrice() != null) {
      product.setPrice(request.getPrice());
    }
    if (request.getStockQuantity() != null) {
      product.setStockQuantity(request.getStockQuantity());
    }
    if (request.getStatus() != null) {
      product.setStatus(request.getStatus());
    }

    return ProductDto.fromEntity(product);
  }

  /**
   * 상품 삭제
   * @param id 상품 ID
   * @param sellerId 판매자 ID
   */
  @Transactional
  public void deleteProduct(Long id, Long sellerId) {
    ProductEntity product = validateProductAndAccess(id, sellerId);
    productRepository.delete(product);
  }

  /**
   * 판매자 유효성을 검사
   * @param sellerId 판매자 ID
   * @param userId 요청한 사용자 ID
   * @return 유효한 판매자 엔티티
   */
  private MemberEntity validateSeller(Long sellerId, Long userId) {
    MemberEntity seller = memberRepository.findById(sellerId)
        .orElseThrow(() -> new CustomException(ErrorCode.SELLER_NOT_FOUND));

    if (!sellerId.equals(userId)) {
      throw new CustomException(ErrorCode.INVALID_SELLER_ACCESS);
    }

    return seller;
  }

  /**
   * 상품 및 접근 유효성 검사
   * @param id 상품 ID
   * @param sellerId 판매자 ID
   * @return 유효한 상품 엔티티
   */
  private ProductEntity validateProductAndAccess(Long id, Long sellerId) {
    ProductEntity product = productRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    if (!product.getSeller().getId().equals(sellerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
    return product;
  }

  /**
   * 상품 엔티티 생성
   * @param request 상품 생성 요청 데이터
   * @param seller 판매자 엔티티
   * @return 생성된 상품 엔티티
   */
  private ProductEntity buildProductEntity(Request request, MemberEntity seller) {
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
