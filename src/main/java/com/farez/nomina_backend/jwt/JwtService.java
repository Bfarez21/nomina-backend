package com.farez.nomina_backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.jsonwebtoken.io.Decoders.BASE64;


@Service
public class JwtService {
    public static final String SECRET_KEY="52020503232362001254784511232125012577895054525";
    public String getToken(UserDetails usuario) {return getToken(new HashMap<>(), usuario);}
    public String getToken(Map<String, Object> extraClaims, UserDetails ususario){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(ususario.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*24))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    //usami s algoritmo de encriptacion
    private Key getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    // obtenemos username por token
    public String getUserNameFromToken(String token){return getClaim(token, Claims::getSubject);}

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username=getUserNameFromToken(token);
        return username.equals(userDetails.getUsername())&& !isTokenExpired(token);
    }

    private Date getExpiration(String token){return getClaim(token, Claims::getExpiration);}
    private boolean isTokenExpired(String token){
        return getExpiration(token).before(new Date());
    }

    private Claims getAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsTFunction)
    {
        final Claims claims=getAllClaims(token);
        return claimsTFunction.apply(claims);
    }
}
