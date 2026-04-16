package com.miproject.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JWTService {

    private static final String SECRET = System.getenv("JWT_SECRET") != null ? 
                                          System.getenv("JWT_SECRET") : "miClaveSecretaSuperSegura123";
    private static final long EXPIRATION_TIME = 86400000;

    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    public static String generarToken(int idUsuario, String nombreUsuario, int idPerfil) {
        return JWT.create()
                .withSubject(String.valueOf(idUsuario))
                .withClaim("nombreUsuario", nombreUsuario)
                .withClaim("idPerfil", idPerfil)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static DecodedJWT validarToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public static Integer getUsuarioIdFromToken(String token) {
        DecodedJWT decoded = validarToken(token);
        if (decoded != null) {
            return Integer.parseInt(decoded.getSubject());
        }
        return null;
    }

    public static Integer getPerfilIdFromToken(String token) {
        DecodedJWT decoded = validarToken(token);
        if (decoded != null) {
            return decoded.getClaim("idPerfil").asInt();
        }
        return null;
    }
}