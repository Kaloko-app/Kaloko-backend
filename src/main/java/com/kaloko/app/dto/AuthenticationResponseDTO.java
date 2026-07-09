package com.kaloko.app.dto;

public record AuthenticationResponseDTO(String token, String refreshToken, UserResponseDTO user) {}
