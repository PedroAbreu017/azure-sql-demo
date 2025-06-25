// JwtTokenUtil.java - CORRIGIDO
package com.example.azure_sql_demo.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    @Value("${app.jwt.issuer:azure-sql-demo}")
    private String jwtIssuer;

    // ========== TOKEN GENERATION ==========

    /**
     * Generate JWT token from username
     */
    public String generateToken(String username) {
        log.debug("Generating JWT token for username: {}", username);
        return createToken(username, jwtExpiration);
    }

    /**
     * Generate JWT token from UserDetails
     */
    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user: {}", userDetails.getUsername());
        return createToken(userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        log.debug("Generating refresh token for username: {}", username);
        return createToken(username, jwtRefreshExpiration);
    }

    /**
     * Generate refresh token from UserDetails
     */
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("Generating refresh token for user: {}", userDetails.getUsername());
        return createToken(userDetails.getUsername(), jwtRefreshExpiration);
    }

    // ========== TOKEN EXTRACTION ==========

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getExpirationTime);
    }

    /**
     * Extract issuer from token
     */
    public String getIssuerFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getIssuer);
    }

    /**
     * Extract issued at date from token
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getIssueTime);
    }

    // ========== TOKEN VALIDATION ==========

    /**
     * Validate token with username
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token with UserDetails
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails.getUsername());
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Check if token is valid (not expired and properly signed)
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token) && verifySignature(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== GETTERS ==========

    /**
     * Get JWT expiration time in milliseconds
     */
    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Get JWT refresh expiration time in milliseconds
     */
    public Long getJwtRefreshExpiration() {
        return jwtRefreshExpiration;
    }

    /**
     * Get JWT expiration time in seconds
     */
    public Long getJwtExpirationInSeconds() {
        return jwtExpiration / 1000;
    }

    /**
     * Get JWT secret (for testing purposes only)
     */
    protected String getJwtSecret() {
        return jwtSecret;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Create JWT token with expiration
     */
    private String createToken(String username, Long expiration) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issuer(jwtIssuer)
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .claim("type", "access_token")
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            JWSSigner signer = new MACSigner(jwtSecret.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException e) {
            log.error("Error creating JWT token: {}", e.getMessage());
            throw new RuntimeException("Error creating JWT token", e);
        }
    }

    /**
     * Extract claim from token
     */
    private <T> T getClaimFromToken(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from token
     */
    private JWTClaimsSet getAllClaimsFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    /**
     * Verify token signature
     */
    private boolean verifySignature(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            log.debug("Token signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== TOKEN UTILITIES ==========

    /**
     * Get token type from claims
     */
    public String getTokenType(String token) {
        try {
            JWTClaimsSet claims = getAllClaimsFromToken(token);
            return claims.getStringClaim("type");
        } catch (Exception e) {
            log.debug("Error getting token type: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if token is refresh token
     */
    public boolean isRefreshToken(String token) {
        return "refresh_token".equals(getTokenType(token));
    }

    /**
     * Check if token is access token
     */
    public boolean isAccessToken(String token) {
        return "access_token".equals(getTokenType(token));
    }

    /**
     * Get remaining time until token expires (in milliseconds)
     */
    public long getRemainingTimeUntilExpiration(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Check if token will expire soon (within 5 minutes)
     */
    public boolean willExpireSoon(String token) {
        long remainingTime = getRemainingTimeUntilExpiration(token);
        return remainingTime < 300000; // 5 minutes in milliseconds
    }
}