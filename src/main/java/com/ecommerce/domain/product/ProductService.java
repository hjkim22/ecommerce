package com.ecommerce.domain.product;

import com.ecommerce.common.enums.ErrorCode;
import com.ecommerce.common.enums.ProductStatus;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.domain.member.Member;
import com.ecommerce.domain.member.MemberRepository;
import com.ecommerce.domain.product.dto.ProductCreateDto;
import com.ecommerce.domain.product.dto.ProductCreateDto.Request;
import com.ecommerce.domain.product.dto.ProductDto;
import com.ecommerce.domain.product.dto.ProductUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  /**
   * 상품 생성
   *
   * @param request  상품 생성 요청 데이터
   * @param sellerId 판매자 ID
   * @return 상품 생성 응답 데이터
   */
  public ProductCreateDto.Response createProduct(ProductCreateDto.Request request, Long sellerId) {
    Member seller = validateSeller(request.getSellerId(), sellerId);
    Product product = buildProductEntity(request, seller);

    return new ProductCreateDto.Response(product.getId(), "상품 등록 완료");
  }

  /**
   * 상품 정보 조회
   *
   * @param productId 상품 ID
   * @return 상품 DTO
   */
  public ProductDto getProductById(Long productId) {
    Product product = findProductById(productId);
    return ProductDto.fromEntity(product);
  }

  /**
   * 상품 정보 조회
   *
   * @param productName 검색할 상품명
   * @param pageable    페이징 정보
   * @return 검색된 상품 목록
   */
  public Page<ProductDto> getProductByName(String productName, Pageable pageable) {
    return getProductPage(productRepository.findByProductNameContaining(productName, pageable));
  }

  /**
   * 상품 정보 조회
   *
   * @param sellerId 판매자 ID
   * @param pageable 페이징 정보
   * @return 검색된 상품 목록
   */
  public Page<ProductDto> getProductBySellerId(Long sellerId, Pageable pageable) {
    memberRepository.findById(sellerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    return getProductPage(productRepository.findBySellerId(sellerId, pageable));
  }

  /**
   * 상품 정보 조회
   *
   * @param status   상품 상태
   * @param pageable 페이징 정보
   * @return 검색된 상품 목록
   */
  public Page<ProductDto> getProductByStatus(ProductStatus status, Pageable pageable) {
    return getProductPage(productRepository.findByStatus(status, pageable));
  }

  /**
   * 전체 상품 리스트 조회
   *
   * @param pageable 페이징 정보
   * @return 전체 상품 목록 - 최신순
   */
  public Page<ProductDto> getAllProducts(Pageable pageable) {
    Pageable sortedByCreatedAtDesc = PageRequest.of(
        pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
    Page<Product> products = productRepository.findAll(sortedByCreatedAtDesc);
    return products.map(ProductDto::fromEntity);
  }

  /**
   * 상품 정보 수정
   *
   * @param productId 수정할 상품 ID
   * @param request   상품 수정 요청 데이터
   * @param sellerId  판매자 ID
   * @return 수정된 상품 DTO
   */
  @Transactional
  public ProductDto updateProduct(Long productId, ProductUpdateDto request, Long sellerId) {
    Product product = validateProductAndAccess(productId, sellerId);
    updateProductFields(request, product);

    return ProductDto.fromEntity(product);
  }

  /**
   * 상품 삭제 (상품 상태를 'DELETED' 로 변경).
   *
   * @param productId 삭제할 상품 ID
   * @param sellerId  판매자 ID
   */
  @Transactional
  public void deleteProduct(Long productId, Long sellerId) {
    Product product = validateProductAndAccess(productId, sellerId);
    product.setStatus(ProductStatus.DELETED);
  }

  // ================================= Helper methods ================================= //

  // 상품 ID로 상품을 조회
  private Product findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  // 상품 엔티티 페이지 형태 DTO 로 변환
  private Page<ProductDto> getProductPage(Page<Product> products) {
    return products.map(ProductDto::fromEntity);
  }

  // 판매자 검증
  private Member validateSeller(Long sellerId, Long requestSellerId) {
    Member seller = memberRepository.findById(sellerId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!sellerId.equals(requestSellerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
    return seller;
  }

  // 상품 접근 권한 검증
  private Product validateProductAndAccess(Long productId, Long sellerId) {
    Product product = findProductById(productId);
    if (!isAdmin() && !product.getSeller().getId().equals(sellerId)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
    }
    return product;
  }

  // 관리자 권한 검증
  private boolean isAdmin() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
  }

  // 상품 엔티티 생성
  private Product buildProductEntity(Request request, Member seller) {
    return productRepository.save(Product.builder()
        .productName(request.getProductName())
        .description(request.getDescription())
        .price(request.getPrice())
        .stockQuantity(request.getStockQuantity())
        .status(request.getStatus()) // 기본값 AVAILABLE
        .seller(seller)
        .build()
    );
  }

  // 상품 필드 업데이트
  private void updateProductFields(ProductUpdateDto request, Product product) {
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
  }
}
