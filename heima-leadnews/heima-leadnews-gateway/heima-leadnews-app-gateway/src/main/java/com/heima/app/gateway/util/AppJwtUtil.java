package com.heima.app.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppJwtUtil {

    private static String TOKEN_ENCRY_KEY;

    @Value("${jwt.secret}")
    public void setTokenEncryKey(String secret) {
        TOKEN_ENCRY_KEY = secret;
    }

    @PostConstruct
    public void init() {
        if (TOKEN_ENCRY_KEY == null || TOKEN_ENCRY_KEY.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Set 'jwt.secret' in environment variables or application.yml");
        }
        log.info("JWT secret loaded successfully");
    }

    public static boolean verifyToken(Claims claims) {
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    private static Jws<Claims> getJws(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(generalKey())
            .build()
            .parseClaimsJws(token);
    }

    public static Claims getClaimsBody(String token) throws ExpiredJwtException {
        return getJws(token).getBody();
    }

    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(TOKEN_ENCRY_KEY);
        return new SecretKeySpec(encodedKey, SignatureAlgorithm.HS512.getJcaName());
    }

}