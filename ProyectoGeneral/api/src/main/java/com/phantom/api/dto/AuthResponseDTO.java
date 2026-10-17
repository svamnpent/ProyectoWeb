package com.phantom.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    /** Segundos hasta que expira el token. */
    private long expiresIn;
}
