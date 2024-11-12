package com.ecommerce.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequestDto(@NotBlank @Email String email) {}