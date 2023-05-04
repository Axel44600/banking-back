package com.apply.banking.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtUtils {

  private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

  private final SecretKey jwtSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public boolean hasClaim(String token, String claimName) {
    final Claims claims = extractAllClaims(token);
    return claims.get(claimName) != null;
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(jwtSigningKey).build().parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails);
  }

  public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
    return createToken(claims, userDetails);
  }

  private String createToken(Map<String, Object> claims, UserDetails userDetails) {
    return Jwts.builder().setClaims(claims)
        .setSubject(userDetails.getUsername())
        .claim("authorities", userDetails.getAuthorities())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(200)))
        .signWith(jwtSigningKey, SignatureAlgorithm.HS256).compact();
  }

  public Boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

}
