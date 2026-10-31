package com.farez.nomina_backend.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
/*
 * busca interceptar solicitudes antes de que lleguen a los controladores y, si se
 * detecta que el JWT ha expirado (a través de una excepción), responde inmediatamente
 * con un error 401 y un mensaje personalizado.*/

@Component
public class JwtExpiredFilter extends WebRequestHandlerInterceptorAdapter {

    public JwtExpiredFilter(WebRequestInterceptor requestInterceptor){
        super(requestInterceptor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        try {
            return super.preHandle(request, response, handler);
        }catch (ExpiredJwtException ex){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "eL TOKEN HA EXPIRADO");
            return false;
        }
    }
}
