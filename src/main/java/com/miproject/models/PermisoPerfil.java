package com.miproject.models;

import java.io.Serializable;

public class PermisoPerfil implements Serializable {
    private int id;
    private int idModulo;
    private int idPerfil;
    private boolean bitAgregar;
    private boolean bitEditar;
    private boolean bitEliminar;
    private boolean bitConsulta;
    private boolean bitDetalle;
    private String fechaRegistro;

    public PermisoPerfil() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdModulo() { return idModulo; }
    public void setIdModulo(int idModulo) { this.idModulo = idModulo; }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public boolean isBitAgregar() { return bitAgregar; }
    public void setBitAgregar(boolean bitAgregar) { this.bitAgregar = bitAgregar; }

    public boolean isBitEditar() { return bitEditar; }
    public void setBitEditar(boolean bitEditar) { this.bitEditar = bitEditar; }

    public boolean isBitEliminar() { return bitEliminar; }
    public void setBitEliminar(boolean bitEliminar) { this.bitEliminar = bitEliminar; }

    public boolean isBitConsulta() { return bitConsulta; }
    public void setBitConsulta(boolean bitConsulta) { this.bitConsulta = bitConsulta; }

    public boolean isBitDetalle() { return bitDetalle; }
    public void setBitDetalle(boolean bitDetalle) { this.bitDetalle = bitDetalle; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}