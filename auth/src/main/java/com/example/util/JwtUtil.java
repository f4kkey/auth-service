package com.example.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.util.Date;

public class JwtUtil {

        private static final Key KEY = Keys
                        .hmacShaKeyFor("MY_SUPER_SECRET_KEY_123456789_MY_SUPER_SECRET_KEY_123456789".getBytes());

        public static String generateToken(String username) {
                return Jwts.builder()
                                .subject(username)
                                .issuedAt(new Date())
                                .expiration(new Date(System.currentTimeMillis() + 86400000))
                                .signWith(KEY)
                                .compact();
        }

        public static String validate(String token) {

                return Jwts.parser()
                                .verifyWith((javax.crypto.SecretKey) KEY)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getSubject();
        }
}
