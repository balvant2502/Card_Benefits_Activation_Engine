package com.viva.benefits_engine.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String email;
    private String name;
    private String role;
    private String token;
    private String message;
}
