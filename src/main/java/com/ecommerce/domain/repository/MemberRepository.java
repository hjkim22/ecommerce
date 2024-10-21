package com.ecommerce.domain.repository;

import com.ecommerce.domain.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

  Optional<MemberEntity> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByPhoneNumber(String phoneNumber);
}
