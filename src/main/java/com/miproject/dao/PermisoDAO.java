package com.miproject.dao;

import com.miproject.models.PermisoPerfil;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    // Guardar permisos (si existe, actualiza; si no, inserta)
    public boolean guardarPermisos(List<PermisoPerfil> permisos) {
        String sql = "INSERT INTO permisos_perfil (idmodulo, idperfil, bitagregar, biteditar, biteliminar, bitconsulta) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (idmodulo, idperfil) DO UPDATE SET " +
                     "bitagregar = EXCLUDED.bitagregar, " +
                     "biteditar = EXCLUDED.biteditar, " +
                     "biteliminar = EXCLUDED.biteliminar, " +
                     "bitconsulta = EXCLUDED.bitconsulta"; 

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
        String sql = "SELECT * FROM permisos_perfil WHERE idperfil = ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPerfil);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PermisoPerfil p = new PermisoPerfil();
                p.setId(rs.getInt("id"));
                p.setIdModulo(rs.getInt("idmodulo"));
                p.setIdPerfil(rs.getInt("idperfil"));
                p.setBitAgregar(rs.getBoolean("bitagregar"));
                p.setBitEditar(rs.getBoolean("biteditar"));
                p.setBitEliminar(rs.getBoolean("biteliminar"));
                p.setBitConsulta(rs.getBoolean("bitconsulta"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Eliminar permisos de un perfil
    public boolean eliminarPorPerfil(int idPerfil) {
        String sql = "DELETE FROM permisos_perfil WHERE idperfil = ?";

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

    public PermisoPerfil obtenerPorPerfilYModulo(int idPerfil, int idModulo) {
        String sql = "SELECT * FROM permisos_perfil WHERE idperfil = ? AND idmodulo = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPerfil);
            ps.setInt(2, idModulo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                PermisoPerfil p = new PermisoPerfil();
                p.setId(rs.getInt("id"));
                p.setIdModulo(rs.getInt("idmodulo"));
                p.setIdPerfil(rs.getInt("idperfil"));
                p.setBitAgregar(rs.getBoolean("bitagregar"));
                p.setBitEditar(rs.getBoolean("biteditar"));
                p.setBitEliminar(rs.getBoolean("biteliminar"));
                p.setBitConsulta(rs.getBoolean("bitconsulta"));
                p.setFechaRegistro(rs.getString("fecha_registro"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}