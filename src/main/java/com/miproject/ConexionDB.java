package com.miproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = System.getenv("DB_URL") != null ? 
        System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/mi_wicket_app";
    
    private static final String USER = System.getenv("DB_USER") != null ? 
        System.getenv("DB_USER") : "postgres";
    
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? 
        System.getenv("DB_PASSWORD") : "1234567";

    public static Connection conectar() {
        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            
            // ✅ SIMPLE Y DIRECTO
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            
            System.out.println("Conexión a PostgreSQL exitosa.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el driver de PostgreSQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos.");
            e.printStackTrace();
        }
        return con;
    }
}