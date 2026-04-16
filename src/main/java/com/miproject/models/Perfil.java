    package com.miproject.models;

    import java.io.Serializable;

    public class Perfil implements Serializable {
        private int id;
        private String strNombrePerfil;
        private boolean bitAdministrador;
        private String fechaRegistro;

        public Perfil() {}

        public Perfil(int id, String strNombrePerfil, boolean bitAdministrador) {
            this.id = id;
            this.strNombrePerfil = strNombrePerfil;
            this.bitAdministrador = bitAdministrador;
        }

        // Getters y Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getStrNombrePerfil() { return strNombrePerfil; }
        public void setStrNombrePerfil(String strNombrePerfil) { this.strNombrePerfil = strNombrePerfil; }

        public boolean isBitAdministrador() { return bitAdministrador; }
        public void setBitAdministrador(boolean bitAdministrador) { this.bitAdministrador = bitAdministrador; }

        public String getFechaRegistro() { return fechaRegistro; }
        public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    }