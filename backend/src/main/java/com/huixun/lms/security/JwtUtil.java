package com.huixun.lms.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long expiresMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expires-in-minutes}") long expiresMinutes) {
        byte[] bytes = secret.length() < 32 ? pad(secret).getBytes() : Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expiresMillis = expiresMinutes * 60 * 1000;
    }

    public String generate(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiresMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        } catch (SignatureException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String pad(String s) {
        StringBuilder b = new StringBuilder(s);
        while (b.length() < 32) b.append('0');
        return b.toString();
    }
}
