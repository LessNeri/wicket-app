package com.miproject;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:wicketdb.sqlite";
    
    static {
        inicializarBD();
    }
    
    private static void inicializarBD() {
        try {
            // Asegurar que el archivo SQLite exista en Render
            java.io.File dbFile = new java.io.File("wicketdb.sqlite");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                System.out.println("Archivo de base de datos creado en: " + dbFile.getAbsolutePath());
            }
            
            // Conectar y crear tabla
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {
                
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "apellido TEXT NOT NULL, " +
                    "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                
                System.out.println("Base de datos inicializada correctamente");
                
            } catch (SQLException e) {
                System.err.println("Error SQL BD: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error inicializando BD: " + e.getMessage());
        }
    }
    
    public static void insertarUsuario(String nombre, String apellido) {
        String sql = "INSERT INTO usuarios (nombre, apellido) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.executeUpdate();
            
            System.out.println("Usuario insertado: " + nombre + " " + apellido);
            
        } catch (SQLException e) {
            System.err.println("Error insertando: " + e.getMessage());
        }
    }
}