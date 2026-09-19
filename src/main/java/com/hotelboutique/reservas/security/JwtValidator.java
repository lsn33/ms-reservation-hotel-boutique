package com.hotelboutique.reservas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final RsaKeyProvider rsaKeyProvider;

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esValido(Claims claims) {
        return claims.getExpiration().after(new Date());
    }
}