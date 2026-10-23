package com.utp.tienda.dto;

public record TokenResponse(
		String tokenType,
		String accessToken,
		long expiresInSeconds) {
}
