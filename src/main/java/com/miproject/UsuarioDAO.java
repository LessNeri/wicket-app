package com.miproject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // 1. CREAR (INSERT)
    public boolean crearUsuario(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, email, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?)";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getTelefono());
            
            // Convertimos el String de fecha a Date de SQL (si viene vacío, mandamos null)
            if (u.getFechaNacimiento() != null && !u.getFechaNacimiento().isEmpty()) {
                ps.setDate(4, Date.valueOf(u.getFechaNacimiento()));
            } else {
                ps.setDate(4, null);
            }

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. LEER TODOS (SELECT)
    public List<Usuario> leerTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id DESC";

        try (Connection con = ConexionDB.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setTelefono(rs.getString("telefono"));
                // Convertimos de vuelta a String para el objeto
                Date fecha = rs.getDate("fecha_nacimiento");
                u.setFechaNacimiento(fecha != null ? fecha.toString() : "");
                
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. ACTUALIZAR (UPDATE)
    public boolean actualizarUsuario(Usuario u) {
        String sql = "UPDATE usuarios SET nombre=?, email=?, telefono=?, fecha_nacimiento=? WHERE id=?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getTelefono());

            if (u.getFechaNacimiento() != null && !u.getFechaNacimiento().isEmpty()) {
                ps.setDate(4, Date.valueOf(u.getFechaNacimiento()));
            } else {
                ps.setDate(4, null);
            }

            ps.setInt(5, u.getId());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. ELIMINAR (DELETE)
    public boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}