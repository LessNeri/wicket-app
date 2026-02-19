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

    // 5. BUSCAR CON FILTROS Y PAGINACIÓN (NUEVO)
public List<Usuario> buscarConFiltros(String search, String fechaDesde, String fechaHasta, int page, int size) {
    List<Usuario> lista = new ArrayList<>();
    
    // Construimos el SQL dinámicamente
    StringBuilder sql = new StringBuilder("SELECT * FROM usuarios WHERE 1=1");
    
    // Filtro por nombre (búsqueda parcial, insensible a mayúsculas)
    if (search != null && !search.trim().isEmpty()) {
        sql.append(" AND LOWER(nombre) LIKE LOWER(?)");
    }
    
    // Filtro por rango de fechas
    if (fechaDesde != null && !fechaDesde.isEmpty()) {
        sql.append(" AND fecha_nacimiento >= ?");
    }
    if (fechaHasta != null && !fechaHasta.isEmpty()) {
        sql.append(" AND fecha_nacimiento <= ?");
    }
    
    // Ordenamiento y paginación
    sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
    
    try (Connection con = ConexionDB.conectar();
         PreparedStatement ps = con.prepareStatement(sql.toString())) {
        
        int index = 1;
        
        // Asignamos los valores según los filtros activos
        if (search != null && !search.trim().isEmpty()) {
            ps.setString(index++, "%" + search + "%");
        }
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            ps.setDate(index++, Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            ps.setDate(index++, Date.valueOf(fechaHasta));
        }
        
        // Límite y offset para paginación
        ps.setInt(index++, size);
        ps.setInt(index++, (page - 1) * size);
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Usuario u = new Usuario();
            u.setId(rs.getInt("id"));
            u.setNombre(rs.getString("nombre"));
            u.setEmail(rs.getString("email"));
            u.setTelefono(rs.getString("telefono"));
            
            Date fecha = rs.getDate("fecha_nacimiento");
            u.setFechaNacimiento(fecha != null ? fecha.toString() : "");
            
            lista.add(u);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}

// 6. CONTAR TOTAL DE RESULTADOS CON FILTROS (NUEVO)
public int contarConFiltros(String search, String fechaDesde, String fechaHasta) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM usuarios WHERE 1=1");
    
    if (search != null && !search.trim().isEmpty()) {
        sql.append(" AND LOWER(nombre) LIKE LOWER(?)");
    }
    if (fechaDesde != null && !fechaDesde.isEmpty()) {
        sql.append(" AND fecha_nacimiento >= ?");
    }
    if (fechaHasta != null && !fechaHasta.isEmpty()) {
        sql.append(" AND fecha_nacimiento <= ?");
    }
    
    try (Connection con = ConexionDB.conectar();
         PreparedStatement ps = con.prepareStatement(sql.toString())) {
        
        int index = 1;
        
        if (search != null && !search.trim().isEmpty()) {
            ps.setString(index++, "%" + search + "%");
        }
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            ps.setDate(index++, Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            ps.setDate(index++, Date.valueOf(fechaHasta));
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