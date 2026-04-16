package com.miproject.dao;

import com.miproject.models.Modulo;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuloDAO {

    // Crear nuevo módulo (con idMenuPadre e idEstado)
    public boolean crear(Modulo modulo) {
        String sql = "INSERT INTO modulos (strNombreModulo, idMenuPadre, idEstado) VALUES (?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, modulo.getStrNombreModulo());
            ps.setInt(2, modulo.getIdMenuPadre());
            ps.setInt(3, modulo.getIdEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener todos los módulos
    public List<Modulo> listarTodos() {
        List<Modulo> lista = new ArrayList<>();
        String sql = "SELECT * FROM modulos ORDER BY id DESC";

        try (Connection con = ConexionDB.conectar();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Modulo m = new Modulo();
                m.setId(rs.getInt("id"));
                m.setStrNombreModulo(rs.getString("strNombreModulo"));
                m.setIdMenuPadre(rs.getInt("idMenuPadre"));
                m.setIdEstado(rs.getInt("idEstado"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Obtener menús principales (idMenuPadre = 0)
    public List<Modulo> obtenerMenusPrincipales() {
        List<Modulo> lista = new ArrayList<>();
        String sql = "SELECT * FROM modulos WHERE idMenuPadre = 0 ORDER BY id";

        try (Connection con = ConexionDB.conectar();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Modulo m = new Modulo();
                m.setId(rs.getInt("id"));
                m.setStrNombreModulo(rs.getString("strNombreModulo"));
                m.setIdMenuPadre(rs.getInt("idMenuPadre"));
                m.setIdEstado(rs.getInt("idEstado"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Verificar si ya existe un módulo con el mismo nombre
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM modulos WHERE strNombreModulo = ?";
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

    // Verificar si ya existe un módulo con el mismo nombre (excluyendo un ID)
    public boolean existeNombreExcepto(String nombre, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM modulos WHERE strNombreModulo = ? AND id != ?";
        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idExcluir);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Buscar con filtros y paginación
    public List<Modulo> buscarConFiltros(String search, int page, int size) {
        List<Modulo> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM modulos WHERE idMenuPadre > 0");
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombreModulo) LIKE LOWER(?)");
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
                Modulo m = new Modulo();
                m.setId(rs.getInt("id"));
                m.setStrNombreModulo(rs.getString("strNombreModulo"));
                m.setIdMenuPadre(rs.getInt("idMenuPadre"));
                m.setIdEstado(rs.getInt("idEstado"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Contar total para paginación
    public int contarConFiltros(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM modulos WHERE idMenuPadre > 0");
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombreModulo) LIKE LOWER(?)");
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

    // Obtener módulo por ID
    public Modulo obtenerPorId(int id) {
        String sql = "SELECT * FROM modulos WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Modulo m = new Modulo();
                m.setId(rs.getInt("id"));
                m.setStrNombreModulo(rs.getString("strNombreModulo"));
                m.setIdMenuPadre(rs.getInt("idMenuPadre"));
                m.setIdEstado(rs.getInt("idEstado"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                return m;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Actualizar módulo
    public boolean actualizar(Modulo modulo) {
        String sql = "UPDATE modulos SET strNombreModulo = ?, idMenuPadre = ?, idEstado = ? WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, modulo.getStrNombreModulo());
            ps.setInt(2, modulo.getIdMenuPadre());
            ps.setInt(3, modulo.getIdEstado());
            ps.setInt(4, modulo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar módulo
    public boolean eliminar(int id) {
        String sql = "DELETE FROM modulos WHERE id = ?";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== MÉTODO PARA VERIFICAR SI UN MÓDULO TIENE SUBMENÚS =====
    public boolean tieneSubmenus(int idModulo) {
        String sql = "SELECT COUNT(*) FROM modulos WHERE idMenuPadre = ?";
        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idModulo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Buscar PADRES con filtros y paginación (idMenuPadre = 0)
    public List<Modulo> buscarPadresConFiltros(String search, int page, int size) {
        List<Modulo> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM modulos WHERE idMenuPadre = 0");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombreModulo) LIKE LOWER(?)");
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
                Modulo m = new Modulo();
                m.setId(rs.getInt("id"));
                m.setStrNombreModulo(rs.getString("strNombreModulo"));
                m.setIdMenuPadre(rs.getInt("idMenuPadre"));
                m.setIdEstado(rs.getInt("idEstado"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Contar total de PADRES para paginación (idMenuPadre = 0)
    public int contarPadresConFiltros(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM modulos WHERE idMenuPadre = 0");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(strNombreModulo) LIKE LOWER(?)");
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

    // Método para obtener los "Encabezados" (Menús que no tienen padre)
    public List<Modulo> obtenerPadres() {
        List<Modulo> lista = new ArrayList<>();
        // Ajusta "idMenuPadre" y "idEstado" según tus columnas reales
        String sql = "SELECT * FROM modulos WHERE idMenuPadre = 0 AND idEstado = 1 ORDER BY id ASC";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearModulo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Método para obtener los módulos que pertenecen a un grupo específico
    public List<Modulo> obtenerHijosPorPadre(int idPadre) {
        List<Modulo> lista = new ArrayList<>();
        String sql = "SELECT * FROM modulos WHERE idMenuPadre = ? AND idEstado = 1 ORDER BY id ASC";

        try (Connection con = ConexionDB.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPadre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearModulo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Modulo mapearModulo(ResultSet rs) throws SQLException {
        Modulo m = new Modulo();
        m.setId(rs.getInt("id"));
        m.setStrNombreModulo(rs.getString("strNombreModulo"));
        m.setIdMenuPadre(rs.getInt("idMenuPadre"));
        m.setIdEstado(rs.getInt("idEstado"));
        m.setFechaRegistro(rs.getString("fecha_registro"));
        return m;
    }

}