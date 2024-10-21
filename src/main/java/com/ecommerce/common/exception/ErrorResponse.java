package com.ecommerce.common.exception;

// 레코드 클래스
public record ErrorResponse(int statusCode, String message) {}