package com.gerolori.fasteat.security;

import com.gerolori.fasteat.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final JwtPrincipalMapper principalMapper;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties, JwtPrincipalMapper principalMapper) {
        this.jwtProperties = jwtProperties;
        this.principalMapper = principalMapper;
        this.signingKey = deriveSigningKey(jwtProperties.secret());
    }

    public String generateAccessToken(AuthPrincipal principal) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(jwtProperties.expirationMs());

        List<String> roles = principal.roles().stream()
                .map(Enum::name)
                .sorted()
                .toList();

        return Jwts.builder()
                .subject(principal.userId().toString())
                .claim("roles", roles)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public AuthPrincipal parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (claims.getIssuedAt() == null || claims.getExpiration() == null) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token temporal claims are missing");
        }

        return principalMapper.fromClaims(claims);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new JwtAuthenticationException("AUTH_TOKEN_EXPIRED", "Authentication token is expired", exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Authentication token is invalid", exception);
        }
    }

    private SecretKey deriveSigningKey(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
