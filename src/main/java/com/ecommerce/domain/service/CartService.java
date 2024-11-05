package com.ecommerce.domain.service;

import com.ecommerce.domain.entity.CartEntity;
import com.ecommerce.domain.entity.MemberEntity;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  // 장바구니 생성
  public void createCartForMember(MemberEntity member) {
    CartEntity cart = CartEntity.builder()
        .customer(member)
        .build();
    cartRepository.save(cart);
  }
}
