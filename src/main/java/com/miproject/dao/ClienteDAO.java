package com.miproject.dao;

import com.miproject.models.Cliente;
import com.miproject.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean crear(Cliente cliente) {
        String sql = "INSERT INTO clientes (strCodigoCliente, strNombre, strTelefono, strEmpresa, idMenuOrigen, idEstado) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getStrCodigoCliente());
            ps.setString(2, cliente.getStrNombre());
            ps.setString(3, cliente.getStrTelefono());
            ps.setString(4, cliente.getStrEmpresa());
            ps.setInt(5, cliente.getIdMenuOrigen());
            ps.setInt(6, cliente.getIdEstado());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET strCodigoCliente = ?, strNombre = ?, strTelefono = ?, " +
                     "strEmpresa = ?, idEstado = ? WHERE id = ?";
        
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getStrCodigoCliente());
            ps.setString(2, cliente.getStrNombre());
            ps.setString(3, cliente.getStrTelefono());
            ps.setString(4, cliente.getStrEmpresa());
            ps.setInt(5, cliente.getIdEstado());
            ps.setInt(6, cliente.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cliente obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearCliente(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Cliente> buscarConFiltros(String filtro, int idMenuOrigen, int pagina, int tamanoPagina) {
        List<Cliente> lista = new ArrayList<>();
        int offset = (pagina - 1) * tamanoPagina;
        String sql = "SELECT * FROM clientes WHERE idMenuOrigen = ? ";
        boolean tieneFiltro = filtro != null && !filtro.trim().isEmpty();

        if (tieneFiltro) {
            sql += "AND (strNombre ILIKE ? OR strCodigoCliente ILIKE ? OR strEmpresa ILIKE ?) ";
        }
        sql += "ORDER BY id DESC LIMIT ? OFFSET ?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setInt(paramIndex++, idMenuOrigen);
            if (tieneFiltro) {
                String busqueda = "%" + filtro.trim() + "%";
                ps.setString(paramIndex++, busqueda);
                ps.setString(paramIndex++, busqueda);
                ps.setString(paramIndex++, busqueda);
            }
            ps.setInt(paramIndex++, tamanoPagina);
            ps.setInt(paramIndex, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarConFiltros(String filtro, int idMenuOrigen) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE idMenuOrigen = ? ";
        boolean tieneFiltro = filtro != null && !filtro.trim().isEmpty();
        if (tieneFiltro) {
            sql += "AND (strNombre ILIKE ? OR strCodigoCliente ILIKE ? OR strEmpresa ILIKE ?)";
        }
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setInt(paramIndex++, idMenuOrigen);
            if (tieneFiltro) {
                String busqueda = "%" + filtro.trim() + "%";
                ps.setString(paramIndex++, busqueda);
                ps.setString(paramIndex++, busqueda);
                ps.setString(paramIndex, busqueda);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { return rs.getInt(1); }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean existeCodigo(String strCodigoCliente, int idExcluido) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE strCodigoCliente = ? AND id != ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, strCodigoCliente);
            ps.setInt(2, idExcluido);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { return rs.getInt(1) > 0; }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setStrCodigoCliente(rs.getString("strCodigoCliente"));
        c.setStrNombre(rs.getString("strNombre"));
        c.setStrTelefono(rs.getString("strTelefono"));
        c.setStrEmpresa(rs.getString("strEmpresa"));
        c.setIdMenuOrigen(rs.getInt("idMenuOrigen"));
        c.setIdEstado(rs.getInt("idEstado"));
        return c;
    }
}