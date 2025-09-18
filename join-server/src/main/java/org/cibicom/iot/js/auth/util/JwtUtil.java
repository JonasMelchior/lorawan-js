package org.cibicom.iot.js.auth.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtUtil {
    private final String secret;
    private long accessTokenValidity = 3600000;
    private final JwtParser jwtParser;
    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";
    private final UserService userService;

    public JwtUtil(UserService userService, @Value("${jwt.secret}") String secret){
        this.userService = userService;
        this.jwtParser = Jwts.parser().setSigningKey(secret);
        this.secret = secret;
    }

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + Duration.ofMillis(accessTokenValidity).toMillis());

        Optional<User> user = userService.findByEmail(email);
        String role = "";
        if (user.isPresent()) {
            if (user.get().getUserType() == UserType.ADMIN) {
                role = "ADMIN,USER";
            }
            else {
                role = "USER";
            }
        }

        claims.put("roles", role);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private Claims parseJwtClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public Claims resolveClaims(String token) {
        if (token != null) {
            return parseJwtClaims(token);
        }
        return null;
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    private List<String> getRoles(Claims claims) {
        return (List<String>) claims.get("roles");
    }


}