package com.veterinaria.veterinariaAnimalVet.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {
    private static final String HMAC = "HmacSHA256";
    private static final long TTL_SECONDS = 8 * 60 * 60;
    private final ObjectMapper json;
    private final String configuredSecret;
    private final boolean production;
    private byte[] secret;

    @Autowired
    public JwtTokenService(ObjectMapper json,
            @Value("${ANIMALVET_JWT_SECRET:}") String configuredSecret,
            @Value("${spring.profiles.active:}") String activeProfiles) {
        this(json, configuredSecret, java.util.Arrays.stream(activeProfiles.split(","))
                .map(String::trim).anyMatch("prod"::equalsIgnoreCase));
    }

    JwtTokenService(ObjectMapper json, String configuredSecret, boolean production) {
        this.json = json;
        this.configuredSecret = configuredSecret;
        this.production = production;
    }

    @PostConstruct
    void initialize() {
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            byte[] bytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) throw new IllegalStateException("ANIMALVET_JWT_SECRET debe tener al menos 32 bytes.");
            secret = bytes;
        } else {
            if (production) throw new IllegalStateException("ANIMALVET_JWT_SECRET es obligatorio en producción.");
            // Desarrollo local sin variable configurada: claves nuevas en cada arranque.
            secret = new byte[32];
            new SecureRandom().nextBytes(secret);
        }
    }

    public String issue(String subject, long tokenVersion) {
        long now = Instant.now().getEpochSecond();
        String id = UUID.randomUUID().toString();
        try {
            String header = encode(json.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String payload = encode(json.writeValueAsBytes(Map.of("sub", subject, "iat", now,
                    "exp", now + TTL_SECONDS, "jti", id, "ver", tokenVersion)));
            String signingInput = header + "." + payload;
            return signingInput + "." + encode(sign(signingInput));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo crear el token de sesión.", exception);
        }
    }

    public TokenIdentity validate(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) return null;
            Map<String, Object> header = json.readValue(decode(parts[0]), new TypeReference<>() { });
            if (!"HS256".equals(header.get("alg"))) return null;
            byte[] expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, Base64.getUrlDecoder().decode(parts[2]))) return null;
            Map<String, Object> claims = json.readValue(decode(parts[1]), new TypeReference<>() { });
            Object subject = claims.get("sub"), expiry = claims.get("exp"), version = claims.get("ver");
            if (!(subject instanceof String username) || username.isBlank()
                    || !(expiry instanceof Number expiresAt) || expiresAt.longValue() <= Instant.now().getEpochSecond()
                    || !(version instanceof Number tokenVersion) || tokenVersion.longValue() < 0) return null;
            return new TokenIdentity(username, tokenVersion.longValue());
        } catch (Exception ignored) {
            return null;
        }
    }

    public record TokenIdentity(String username, long version) { }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC);
        mac.init(new SecretKeySpec(secret, HMAC));
        return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private byte[] decode(String value) { return Base64.getUrlDecoder().decode(value); }
}
