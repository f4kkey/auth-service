package com.example.util;

import com.example.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.util.Date;

public class JwtUtil {

        private static final SecretKey KEY = Keys.hmacShaKeyFor(
                "MY_SUPER_SECRET_KEY_123456789_MY_SUPER_SECRET_KEY_123456789".getBytes(StandardCharsets.UTF_8)
        );

        public static String generateToken(User user) {
                return Jwts.builder()
                        .subject(user.getUsername())
                        .issuer("my-auth-issuer")
                        .issuedAt(new Date())
                        .claim("id", user.getId())
                        .claim("role", user.getRole())
                        .expiration(new Date(System.currentTimeMillis() + 86400000))
                        .signWith(KEY, Jwts.SIG.HS256)
                        .compact();
        }

        public static String validate(String token) {
                return Jwts.parser()
                        .verifyWith(KEY)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject();
        }
}
