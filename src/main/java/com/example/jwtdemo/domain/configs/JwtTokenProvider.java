package com.example.jwtdemo.domain.configs;

import com.example.jwtdemo.domain.entities.CustomUserDetails;
import com.example.jwtdemo.domain.entities.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {
    // gen JWT_SECRET
    private String JWT_SECRET = "duong";

    // set time jwt
    private long JWT_EXPIRATION = 2 * 3600 * 1000;

     //gen jwt from user info
    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        // gen jwt from user id
        return Jwts.builder()
                .setHeader(header())
                .setClaims(getClaims(userDetails.getUser()))
                .setSubject(Long.toString(userDetails.getUser().getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();

    }


    // get user info from jwt
    public int getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        return Integer.parseInt(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    private Map<String, Object> getClaims(User user){
        Map<String, Object> mClaims = new HashMap<>();
        mClaims.put("email", user.getEmail());
        mClaims.put("role", user.getRole());
        mClaims.put("state", user.getState());
        return mClaims;
    }

    private Map<String, Object> header(){
        Map<String, Object> map = new HashMap<>();
        map.put("typ", "JWT");
        return map;
    }

}
