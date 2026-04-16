package com.miproject.dao;

import com.miproject.models.Perfil;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerfilDAO {

    // Crear nuevo perfil
    public boolean crear(Perfil perfil) {
        String sql = "INSERT INTO perfiles (strNombrePerfil, bitAdministrador) VALUES (?, ?)";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, perfil.getStrNombrePerfil());
            ps.setBoolean(2, perfil.isBitAdministrador());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Listar todos los perfiles
    public List<Perfil> listarTodos() {
        List<Perfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM perfiles ORDER BY id DESC";

        try (Connection con = ConexionDB.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Perfil p = new Perfil();
                p.setId(rs.getInt("id"));
                p.setStrNombrePerfil(rs.getString("strNombrePerfil"));
                p.setBitAdministrador(rs.getBoolean("bitAdministrador"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Buscar con filtros y paginación
    public List<Perfil> buscarConFiltros(String search, int page, int size) {
        List<Perfil> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM perfiles WHERE 1=1");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombrePerfil) LIKE LOWER(?)");
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (search != null && !search.trim().isEmpty()) {
                ps.setString(index++, "%" + search + "%");
            }
            ps.setInt(index++, size);
            ps.setInt(index++, (page - 1) * size);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Perfil p = new Perfil();
                p.setId(rs.getInt("id"));
                p.setStrNombrePerfil(rs.getString("strNombrePerfil"));
                p.setBitAdministrador(rs.getBoolean("bitAdministrador"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Contar total para paginación
    public int contarConFiltros(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM perfiles WHERE 1=1");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombrePerfil) LIKE LOWER(?)");
        }

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (search != null && !search.trim().isEmpty()) {
                ps.setString(1, "%" + search + "%");
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

    // Verificar si ya existe un perfil con el mismo nombre
public boolean existeNombre(String nombre) {
    String sql = "SELECT COUNT(*) FROM perfiles WHERE strNombrePerfil = ?";
    try (Connection con = ConexionDB.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, nombre);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    // Obtener perfil por ID
    public Perfil obtenerPorId(int id) {
        String sql = "SELECT * FROM perfiles WHERE id = ?";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Perfil p = new Perfil();
                p.setId(rs.getInt("id"));
                p.setStrNombrePerfil(rs.getString("strNombrePerfil"));
                p.setBitAdministrador(rs.getBoolean("bitAdministrador"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Actualizar perfil
    public boolean actualizar(Perfil perfil) {
        String sql = "UPDATE perfiles SET strNombrePerfil = ?, bitAdministrador = ? WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, perfil.getStrNombrePerfil());
            ps.setBoolean(2, perfil.isBitAdministrador());
            ps.setInt(3, perfil.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar perfil
    public boolean eliminar(int id) {
        String sql = "DELETE FROM perfiles WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== MÉTODO PARA VERIFICAR USUARIOS ASOCIADOS =====
public boolean tieneUsuariosAsociados(int idPerfil) {
    String sql = "SELECT COUNT(*) FROM usuarios WHERE idPerfil = ?";
    try (Connection con = ConexionDB.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idPerfil);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
}