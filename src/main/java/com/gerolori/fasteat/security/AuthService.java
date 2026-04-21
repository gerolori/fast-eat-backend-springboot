package com.gerolori.fasteat.security;

import com.gerolori.fasteat.domain.entity.RefreshToken;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.RefreshTokenRepository;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import com.gerolori.fasteat.web.error.BusinessRuleViolationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResult register(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessRuleViolationException("AUTH_IDENTITY_CONFLICT", "An account with this email already exists");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER)));

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().add(customerRole);

        User persisted = userRepository.save(user);
        return issueTokens(persisted);
    }

    @Transactional
    public AuthResult login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new JwtAuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new JwtAuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String refreshTokenRaw) {
        RefreshToken refreshToken = resolveActiveRefreshToken(refreshTokenRaw);
        Instant now = Instant.now();

        if (refreshToken.isExpired(now)) {
            refreshToken.setRevokedAt(now);
            refreshTokenRepository.save(refreshToken);
            throw new JwtAuthenticationException("AUTH_TOKEN_EXPIRED", "Refresh token is expired");
        }

        refreshToken.setRevokedAt(now);
        refreshTokenRepository.save(refreshToken);

        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenRaw) {
        String tokenHash = hashToken(refreshTokenRaw);
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResult issueTokens(User user) {
        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        AuthPrincipal principal = new AuthPrincipal(user.getId(), roles);
        String accessToken = jwtTokenService.generateAccessToken(principal);

        String refreshTokenRaw = generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(refreshTokenRaw));
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_TTL));
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(accessToken, refreshTokenRaw, principal.userId(), principal.roles());
    }

    private RefreshToken resolveActiveRefreshToken(String refreshTokenRaw) {
        String tokenHash = hashToken(refreshTokenRaw);
        return refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Refresh token is invalid"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOpaqueToken() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    private String hashToken(String refreshTokenRaw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshTokenRaw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    public record AuthResult(String accessToken, String refreshToken, UUID userId, Set<RoleName> roles) {
    }
}
