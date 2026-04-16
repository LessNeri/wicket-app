package com.miproject.services;

import com.miproject.dao.UsuarioDAO;
import com.miproject.models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private static UsuarioDAO usuarioDAO = new UsuarioDAO();

public static Usuario validarLogin(String correo, String password) throws Exception {
        System.out.println("\n--- INICIANDO DEBUG DE LOGIN ---");
        
        Usuario usuario = usuarioDAO.obtenerPorCorreo(correo); 
        
        if (usuario == null) {
            System.out.println("ERROR: El correo no existe.");
            throw new Exception("El correo ingresado no existe en el sistema.");
        }

        System.out.println("ÉXITO: Usuario encontrado -> " + usuario.getStrNombreUsuario());

        if (usuario.getIdEstadoUsuario() != 1) {
            System.out.println("ERROR: El usuario está inactivo.");
            throw new Exception("Este usuario se encuentra INACTIVO. Por favor, contacta al administrador.");
        }

        try {
            if (BCrypt.checkpw(password, usuario.getStrPwd())) {
                System.out.println("ÉXITO TOTAL: Las contraseñas coinciden.");
                return usuario;
            } else {
                System.out.println("ERROR: Contraseña incorrecta.");
                throw new Exception("La contraseña es incorrecta.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR CRÍTICO: La contraseña en BD no tiene formato BCrypt.");
            throw new Exception("Error de seguridad con la cuenta. Contacte soporte.");
        }
    }
    public static String encriptarPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}