package com.utp.semana4_api_rest.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds) {
}