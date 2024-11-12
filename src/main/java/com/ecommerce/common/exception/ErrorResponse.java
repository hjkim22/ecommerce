package com.ecommerce.common.exception;

public record ErrorResponse(int statusCode, String message) {}