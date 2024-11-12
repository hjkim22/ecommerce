package com.ecommerce.domain.service;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.dto.product.ProductCreateDto;
import com.ecommerce.domain.dto.product.ProductCreateDto.Request;
import com.ecommerce.domain.dto.product.ProductDto;
import com.ecommerce.domain.dto.product.ProductUpdateDto;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.entity.ProductEntity;
import com.ecommerce.domain.repository.MemberRepository;
import com.ecommerce.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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
    ProductEntity product = findProductById(id);

    return ProductDto.fromEntity(product);
  }

  /**
   * 상품 정보 조회
   * @param name 상품명
   * @return 상품 DTO 리스트
   */
  public Page<ProductDto> getProductByName(String name, Pageable pageable) {
    return getProductPage(productRepository.findByProductNameContaining(name, pageable));
  }

  /**
   * 상품 정보 조회
   * @param id 판매자 ID
   * @return 상품 DTO 리스트
   */
  public Page<ProductDto> getProductBySellerId(Long id, Pageable pageable) {
    memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    return getProductPage(productRepository.findBySellerId(id, pageable));
  }

  /**
   * 상품 정보 조회
   * @param status 상품 상태
   * @return 상품 DTO 리스트
   */
  public Page<ProductDto> getProductByStatus(ProductStatus status, Pageable pageable) {
    return getProductPage(productRepository.findByStatus(status, pageable));
  }

  /**
   * 상품 리스트 조회
   * @param pageable 페이징 정보
   * @return 상품 DTO 리스트
   */
  public Page<ProductDto> getProducts(Pageable pageable) {
    return getProductPage(productRepository.findAll(pageable));
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
    updateProductFields(request, product);

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
    product.setStatus(ProductStatus.DELETED);
  }

  // ========================== 헬퍼 메서드 ==========================

  private ProductEntity findProductById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  private Page<ProductDto> getProductPage(Page<ProductEntity> products) {
    return products.map(ProductDto::fromEntity);
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
    ProductEntity product = findProductById(id);
    if (!isAdmin() && !product.getSeller().getId().equals(sellerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
    return product;
  }

  // 어드민 확인
  private boolean isAdmin() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
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

  private void updateProductFields(ProductUpdateDto request, ProductEntity product) {
    if (request.getProductName() != null) product.setProductName(request.getProductName());
    if (request.getDescription() != null) product.setDescription(request.getDescription());
    if (request.getPrice() != null) product.setPrice(request.getPrice());
    if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
    if (request.getStatus() != null) product.setStatus(request.getStatus());
  }
}
