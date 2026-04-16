package com.miproject.filters;

import com.miproject.services.JWTService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class JWTAuthenticationFilter implements IRequestCycleListener {

    private static final String[] PUBLIC_URLS = {
        "/",
        "/login",
        "/error/404"
    };

    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {
        return null;
    }

    @Override
    public void onBeginRequest(RequestCycle cycle) {
        WebRequest request = (WebRequest) cycle.getRequest();
        String url = request.getUrl().toString();
        
        // 1. Estandarizar la URL (Wicket a veces omite la barra inicial)
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        
        // 2. Si es URL pública o archivo estático, permitir acceso sin token
        if (esUrlPublica(url)) {
            return;
        }

        // 3. Obtener token de la cookie
        String token = obtenerTokenDeCookie(request);

        // 4. Validar token
        if (token == null || JWTService.validarToken(token) == null) {
            throw new RestartResponseException(com.miproject.pages.LoginPage.class);
        }
    }

    private boolean esUrlPublica(String url) {
        // A) Permitir SIEMPRE los recursos internos de Wicket, CSS, JS e Iconos
        if (url.startsWith("/wicket/") || url.contains(".css") || url.contains(".js") || url.contains(".ico")) {
            return true;
        }

        // B) Permitir las URLs definidas en el arreglo
        for (String publicUrl : PUBLIC_URLS) {
            // Permitir si es exactamente la raíz "/", o si empieza con "/login"
            if (url.equals(publicUrl) || (publicUrl.length() > 1 && url.startsWith(publicUrl))) {
                return true;
            }
        }
        
        // C) Permitir acceso a carpetas de subidas e imágenes
        if (url.startsWith("/uploads/") || url.startsWith("/images/")) {
            return true;
        }
        
        return false;
    }

    private String obtenerTokenDeCookie(WebRequest request) {
        HttpServletRequest servletRequest = (HttpServletRequest) request.getContainerRequest();
        Cookie[] cookies = servletRequest.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}