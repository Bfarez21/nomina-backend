package com.farez.nomina_backend.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
 * captura de manera global la excepción que se lanza cuando un token JWT expiró y responde
 * con un mensaje HTTP 401 y un mensaje personalizado,
 * */
@ControllerAdvice
public class JwtExpiredExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleJwtExpiredException (ExpiredJwtException ex){
        String errorMessage= "El token ha expirado" + ex.getMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }
}
