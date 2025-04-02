package com.ndifreke.core_banking_api.util;

import com.ndifreke.core_banking_api.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

/**
 * The type Jwt util.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    /**
     * Extract username string.
     *
     * @param token the token
     * @return the string
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract claim t.
     *
     * @param <T>            the type parameter
     * @param token          the token
     * @param claimsResolver the claims resolver
     * @return the t
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract user id uuid.
     *
     * @param token the token
     * @return the uuid
     */
    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String userIdString = claims.get("userId", String.class);
        if (userIdString != null) {
            return UUID.fromString(userIdString);
        } else {
            return null;
        }
    }

    /**
     * Generate token string.
     *
     * @param userDetails the user details
     * @return the string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            claims.put("firstName", customUserDetails.getFirstName());
            claims.put("lastName", customUserDetails.getLastName());
            claims.put("userId", customUserDetails.getUser().getUserId().toString()); // Add this line
        }

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Is token valid boolean.
     *
     * @param token    the token
     * @param username the username
     * @return the boolean
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Gets token from request.
     *
     * @param request the request
     * @return the token from request
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

    private Key getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        Key key = Keys.hmacShaKeyFor(decodedKey);
        return key;
    }

    private Claims extractAllClaims(String token) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(decodedKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validate token boolean.
     *
     * @param token       the token
     * @param userDetails the user details
     * @return the boolean
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isTokenExpired(token));
    }
}