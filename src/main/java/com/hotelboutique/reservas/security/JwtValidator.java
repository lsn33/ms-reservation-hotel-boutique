package com.hotelboutique.reservas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// NOTA: por ahora sigue usando HS256 (mismo secreto que auth-service).
// En el siguiente paso este servicio solo tendra la clave PUBLICA RSA, y ya no podra firmar nada,
// solo verificar tokens firmados por auth-service.
@Component
public class JwtValidator {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esValido(Claims claims) {
        return claims.getExpiration().after(new Date());
    }
}
