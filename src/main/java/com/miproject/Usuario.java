package com.miproject;

import java.io.Serializable;

public class Usuario implements Serializable {
    private int id;
    private String nombre;
    private String email;
    private String telefono;
    private String fechaNacimiento;
    private String archivoRuta;

    public Usuario() {}

    public Usuario(int id, String nombre, String email, String telefono, String fechaNacimiento) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getArchivoRuta() { return archivoRuta; }
    public void setArchivoRuta(String archivoRuta) { this.archivoRuta = archivoRuta; }
}