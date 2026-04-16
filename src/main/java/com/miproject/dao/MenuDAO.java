package com.miproject.dao;

import com.miproject.models.Menu;
import com.miproject.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public List<Menu> listarTodos() {
        List<Menu> lista = new ArrayList<>();
        String sql = "SELECT * FROM menu ORDER BY idMenu, id";

        try (Connection con = ConexionDB.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Menu m = new Menu();
                m.setId(rs.getInt("id"));
                m.setIdMenu(rs.getInt("idMenu"));
                m.setIdModulo(rs.getInt("idModulo"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Menu> obtenerMenusPrincipales() {
        List<Menu> lista = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE idMenu = 0 ORDER BY id";

        try (Connection con = ConexionDB.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Menu m = new Menu();
                m.setId(rs.getInt("id"));
                m.setIdMenu(rs.getInt("idMenu"));
                m.setIdModulo(rs.getInt("idModulo"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Menu> obtenerSubmenus(int idMenu) {
        List<Menu> lista = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE idMenu = ? ORDER BY id";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMenu);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Menu m = new Menu();
                m.setId(rs.getInt("id"));
                m.setIdMenu(rs.getInt("idMenu"));
                m.setIdModulo(rs.getInt("idModulo"));
                m.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}