package com.miproject.models;

import java.io.Serializable;

public class Usuario implements Serializable {
    private int id;
    private String strNombreUsuario;
    private String strApellidoPaterno;
    private String strApellidoMaterno;
    private java.sql.Date fechaNacimiento;
    private int idPerfil;
    private String strPwd;
    private int idEstadoUsuario; 
    private String strCorreo;
    private String strNumeroCelular;
    private String strImagenUrl;
    private String fechaRegistro;

    public Usuario() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStrNombreUsuario() { return strNombreUsuario; }
    public void setStrNombreUsuario(String strNombreUsuario) { this.strNombreUsuario = strNombreUsuario; }

    // --- NUEVOS GETTERS Y SETTERS ---
    public String getStrApellidoPaterno() { return strApellidoPaterno; }
    public void setStrApellidoPaterno(String strApellidoPaterno) { this.strApellidoPaterno = strApellidoPaterno; }

    public String getStrApellidoMaterno() { return strApellidoMaterno; }
    public void setStrApellidoMaterno(String strApellidoMaterno) { this.strApellidoMaterno = strApellidoMaterno; }

    public java.sql.Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(java.sql.Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public String getStrPwd() { return strPwd; }
    public void setStrPwd(String strPwd) { this.strPwd = strPwd; }

    public int getIdEstadoUsuario() { return idEstadoUsuario; }
    public void setIdEstadoUsuario(int idEstadoUsuario) { this.idEstadoUsuario = idEstadoUsuario; }

    public String getStrCorreo() { return strCorreo; }
    public void setStrCorreo(String strCorreo) { this.strCorreo = strCorreo; }

    public String getStrNumeroCelular() { return strNumeroCelular; }
    public void setStrNumeroCelular(String strNumeroCelular) { this.strNumeroCelular = strNumeroCelular; }

    public String getStrImagenUrl() { return strImagenUrl; }
    public void setStrImagenUrl(String strImagenUrl) { this.strImagenUrl = strImagenUrl; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}