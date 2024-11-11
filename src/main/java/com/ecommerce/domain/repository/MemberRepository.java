package com.ecommerce.domain.repository;

import com.ecommerce.common.enums.Role;
import com.ecommerce.domain.entity.MemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

  Optional<MemberEntity> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByPhoneNumber(String phoneNumber);
  boolean existsByPhoneNumberAndIdNot(String phone, Long id);
  Page<MemberEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
  Page<MemberEntity> findMemberByRole(Role role, Pageable pageable);
}
