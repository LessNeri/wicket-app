package com.miproject.models;

import java.io.Serializable;

public class Cliente implements Serializable {
    private int id;
    private String strCodigoCliente;
    private String strNombre;
    private String strTelefono;
    private String strEmpresa;  
    private int idMenuOrigen; 
    private int idEstado;  
    private String fechaRegistro;

    public Cliente() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStrCodigoCliente() { return strCodigoCliente; }
    public void setStrCodigoCliente(String strCodigoCliente) { this.strCodigoCliente = strCodigoCliente; }

    public String getStrNombre() { return strNombre; }
    public void setStrNombre(String strNombre) { this.strNombre = strNombre; }

    public String getStrTelefono() { return strTelefono; }
    public void setStrTelefono(String strTelefono) { this.strTelefono = strTelefono; }

    public String getStrEmpresa() { return strEmpresa; }
    public void setStrEmpresa(String strEmpresa) { this.strEmpresa = strEmpresa; }

    public int getIdMenuOrigen() { return idMenuOrigen; }
    public void setIdMenuOrigen(int idMenuOrigen) { this.idMenuOrigen = idMenuOrigen; }

    public int getIdEstado() { return idEstado; }
    public void setIdEstado(int idEstado) { this.idEstado = idEstado; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}