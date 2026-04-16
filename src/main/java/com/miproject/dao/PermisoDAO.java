package com.miproject.dao;

import com.miproject.models.PermisoPerfil;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    // Guardar permisos (si existe, actualiza; si no, inserta)
    public boolean guardarPermisos(List<PermisoPerfil> permisos) {
        String sql = "INSERT INTO permisos_perfil (idModulo, idPerfil, bitAgregar, bitEditar, bitEliminar, bitConsulta) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (idModulo, idPerfil) DO UPDATE SET " +
                     "bitAgregar = EXCLUDED.bitAgregar, " +
                     "bitEditar = EXCLUDED.bitEditar, " +
                     "bitEliminar = EXCLUDED.bitEliminar, " +
                     "bitConsulta = EXCLUDED.bitConsulta"; 

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (PermisoPerfil p : permisos) {
                ps.setInt(1, p.getIdModulo());
                ps.setInt(2, p.getIdPerfil());
                ps.setBoolean(3, p.isBitAgregar());
                ps.setBoolean(4, p.isBitEditar());
                ps.setBoolean(5, p.isBitEliminar());
                ps.setBoolean(6, p.isBitConsulta());
                ps.addBatch();
            }

            int[] resultados = ps.executeBatch();
            return resultados.length == permisos.size();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Obtener permisos por perfil
    public List<PermisoPerfil> obtenerPorPerfil(int idPerfil) {
        List<PermisoPerfil> lista = new ArrayList<>();
        String sql = "SELECT * FROM permisos_perfil WHERE idPerfil = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPerfil);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PermisoPerfil p = new PermisoPerfil();
                p.setId(rs.getInt("id"));
                p.setIdModulo(rs.getInt("idModulo"));
                p.setIdPerfil(rs.getInt("idPerfil"));
                p.setBitAgregar(rs.getBoolean("bitAgregar"));
                p.setBitEditar(rs.getBoolean("bitEditar"));
                p.setBitEliminar(rs.getBoolean("bitEliminar"));
                p.setBitConsulta(rs.getBoolean("bitConsulta"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Eliminar permisos de un perfil (útil antes de reinsertar)
    public boolean eliminarPorPerfil(int idPerfil) {
        String sql = "DELETE FROM permisos_perfil WHERE idPerfil = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPerfil);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener permisos de un perfil para un módulo específico
public PermisoPerfil obtenerPorPerfilYModulo(int idPerfil, int idModulo) {
    String sql = "SELECT * FROM permisos_perfil WHERE idPerfil = ? AND idModulo = ?";
    try (Connection con = ConexionDB.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idPerfil);
        ps.setInt(2, idModulo);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            PermisoPerfil p = new PermisoPerfil();
            p.setId(rs.getInt("id"));
            p.setIdModulo(rs.getInt("idModulo"));
            p.setIdPerfil(rs.getInt("idPerfil"));
            p.setBitAgregar(rs.getBoolean("bitAgregar"));
            p.setBitEditar(rs.getBoolean("bitEditar"));
            p.setBitEliminar(rs.getBoolean("bitEliminar"));
            p.setBitConsulta(rs.getBoolean("bitConsulta"));
            p.setFechaRegistro(rs.getString("fecha_registro"));
            return p;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
}