package com.miproject.models;

import java.io.Serializable;

public class Modulo implements Serializable {
    private int id;
    private String strNombreModulo;
    private int idMenuPadre;
    private int idEstado;
    private String fechaRegistro;

    public Modulo() {}

    public Modulo(int id, String strNombreModulo) {
        this.id = id;
        this.strNombreModulo = strNombreModulo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStrNombreModulo() { return strNombreModulo; }
    public void setStrNombreModulo(String strNombreModulo) { this.strNombreModulo = strNombreModulo; }

    public int getIdMenuPadre() { return idMenuPadre; }
    public void setIdMenuPadre(int idMenuPadre) { this.idMenuPadre = idMenuPadre; }

    public int getIdEstado() { return idEstado; }
    public void setIdEstado(int idEstado) { this.idEstado = idEstado; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return strNombreModulo;
    }
}