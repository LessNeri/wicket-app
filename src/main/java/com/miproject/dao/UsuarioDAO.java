package com.miproject.dao;

import com.miproject.models.Usuario;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Crear nuevo usuario
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (strNombreUsuario, strApellidoPaterno, strApellidoMaterno, idPerfil, strPwd, idEstadoUsuario, strCorreo, strNumeroCelular, strImagenUrl, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getStrNombreUsuario());
            ps.setString(2, usuario.getStrApellidoPaterno()); 
            ps.setString(3, usuario.getStrApellidoMaterno()); 
            ps.setInt(4, usuario.getIdPerfil());
            ps.setString(5, usuario.getStrPwd()); 
            ps.setInt(6, usuario.getIdEstadoUsuario());
            ps.setString(7, usuario.getStrCorreo());
            ps.setString(8, usuario.getStrNumeroCelular());
            ps.setString(9, usuario.getStrImagenUrl());
            ps.setDate(10, usuario.getFechaNacimiento()); 
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Listar todos los usuarios
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id DESC";

        try (Connection con = ConexionDB.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setStrNombreUsuario(rs.getString("strNombreUsuario"));
                u.setStrApellidoPaterno(rs.getString("strApellidoPaterno"));
                u.setStrApellidoMaterno(rs.getString("strApellidoMaterno"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setIdPerfil(rs.getInt("idPerfil"));
                u.setStrPwd(rs.getString("strPwd"));
                u.setIdEstadoUsuario(rs.getInt("idEstadoUsuario"));
                u.setStrCorreo(rs.getString("strCorreo"));
                u.setStrNumeroCelular(rs.getString("strNumeroCelular"));
                u.setStrImagenUrl(rs.getString("strImagenUrl"));
                u.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

public List<Usuario> buscarConFiltros(String search, int page, int size) {
        List<Usuario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM usuarios WHERE 1=1");

        // CAMBIO AQUÍ: Agregamos el OR para strNumeroCelular
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(strNombreUsuario) LIKE LOWER(?) OR LOWER(strApellidoPaterno) LIKE LOWER(?) OR LOWER(strApellidoMaterno) LIKE LOWER(?) OR LOWER(strCorreo) LIKE LOWER(?) OR LOWER(strNumeroCelular) LIKE LOWER(?))");
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (search != null && !search.trim().isEmpty()) {
                ps.setString(index++, "%" + search + "%"); // 1. Nombre
                ps.setString(index++, "%" + search + "%"); // 2. Apellido Paterno
                ps.setString(index++, "%" + search + "%"); // 3. Apellido Materno
                ps.setString(index++, "%" + search + "%"); // 4. Correo
                ps.setString(index++, "%" + search + "%"); // 5. Celular (NUEVO)
            }
            ps.setInt(index++, size);
            ps.setInt(index++, (page - 1) * size);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setStrNombreUsuario(rs.getString("strNombreUsuario"));
                u.setStrApellidoPaterno(rs.getString("strApellidoPaterno"));
                u.setStrApellidoMaterno(rs.getString("strApellidoMaterno"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setIdPerfil(rs.getInt("idPerfil"));
                u.setStrPwd(rs.getString("strPwd"));
                u.setIdEstadoUsuario(rs.getInt("idEstadoUsuario"));
                u.setStrCorreo(rs.getString("strCorreo"));
                u.setStrNumeroCelular(rs.getString("strNumeroCelular"));
                u.setStrImagenUrl(rs.getString("strImagenUrl"));
                u.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Contar total para paginación
    public int contarConFiltros(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM usuarios WHERE 1=1");

        // CAMBIO AQUÍ: Agregamos el OR para strNumeroCelular
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(strNombreUsuario) LIKE LOWER(?) OR LOWER(strApellidoPaterno) LIKE LOWER(?) OR LOWER(strApellidoMaterno) LIKE LOWER(?) OR LOWER(strCorreo) LIKE LOWER(?) OR LOWER(strNumeroCelular) LIKE LOWER(?))");
        }

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (search != null && !search.trim().isEmpty()) {
                ps.setString(1, "%" + search + "%"); // 1. Nombre
                ps.setString(2, "%" + search + "%"); // 2. Apellido Paterno
                ps.setString(3, "%" + search + "%"); // 3. Apellido Materno
                ps.setString(4, "%" + search + "%"); // 4. Correo
                ps.setString(5, "%" + search + "%"); // 5. Celular (NUEVO)
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Obtener usuario por ID
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setStrNombreUsuario(rs.getString("strNombreUsuario"));
                u.setStrApellidoPaterno(rs.getString("strApellidoPaterno"));
                u.setStrApellidoMaterno(rs.getString("strApellidoMaterno"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setIdPerfil(rs.getInt("idPerfil"));
                u.setStrPwd(rs.getString("strPwd"));
                u.setIdEstadoUsuario(rs.getInt("idEstadoUsuario"));
                u.setStrCorreo(rs.getString("strCorreo"));
                u.setStrNumeroCelular(rs.getString("strNumeroCelular"));
                u.setStrImagenUrl(rs.getString("strImagenUrl"));
                u.setFechaRegistro(rs.getString("fecha_registro"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener usuario por nombre de usuario (para login)
    public Usuario obtenerPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT * FROM usuarios WHERE strNombreUsuario = ?";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setStrNombreUsuario(rs.getString("strNombreUsuario"));
                u.setStrApellidoPaterno(rs.getString("strApellidoPaterno"));
                u.setStrApellidoMaterno(rs.getString("strApellidoMaterno"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setIdPerfil(rs.getInt("idPerfil"));
                u.setStrPwd(rs.getString("strPwd"));
                u.setIdEstadoUsuario(rs.getInt("idEstadoUsuario"));
                u.setStrCorreo(rs.getString("strCorreo"));
                u.setStrNumeroCelular(rs.getString("strNumeroCelular"));
                u.setStrImagenUrl(rs.getString("strImagenUrl"));
                u.setFechaRegistro(rs.getString("fecha_registro"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener usuario por correo electrónico (para login)
    public Usuario obtenerPorCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE strCorreo = ?";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setStrNombreUsuario(rs.getString("strNombreUsuario"));
                u.setStrApellidoPaterno(rs.getString("strApellidoPaterno"));
                u.setStrApellidoMaterno(rs.getString("strApellidoMaterno"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setIdPerfil(rs.getInt("idPerfil"));
                u.setStrPwd(rs.getString("strPwd"));
                u.setIdEstadoUsuario(rs.getInt("idEstadoUsuario"));
                u.setStrCorreo(rs.getString("strCorreo"));
                u.setStrNumeroCelular(rs.getString("strNumeroCelular"));
                u.setStrImagenUrl(rs.getString("strImagenUrl"));
                u.setFechaRegistro(rs.getString("fecha_registro"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Actualizar usuario
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET strNombreUsuario = ?, strApellidoPaterno = ?, strApellidoMaterno = ?, idPerfil = ?, strPwd = ?, idEstadoUsuario = ?, strCorreo = ?, strNumeroCelular = ?, strImagenUrl = ?, fecha_nacimiento = ? WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getStrNombreUsuario());
            ps.setString(2, usuario.getStrApellidoPaterno()); 
            ps.setString(3, usuario.getStrApellidoMaterno()); 
            ps.setInt(4, usuario.getIdPerfil());
            ps.setString(5, usuario.getStrPwd());
            ps.setInt(6, usuario.getIdEstadoUsuario());
            ps.setString(7, usuario.getStrCorreo());
            ps.setString(8, usuario.getStrNumeroCelular());
            ps.setString(9, usuario.getStrImagenUrl());
            ps.setDate(10, usuario.getFechaNacimiento());
            ps.setInt(11, usuario.getId()); 

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar usuario
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}