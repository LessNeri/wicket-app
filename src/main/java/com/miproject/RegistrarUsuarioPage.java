package com.miproject;

import java.util.List;

public class RegistrarUsuarioPage extends BasePage {
    
    public RegistrarUsuarioPage() {
        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Gestión Usuarios", GestionUsuariosPage.class),
            new BreadcrumbItem("Registrar", RegistrarUsuarioPage.class)
        ));
        
        // Esta página será principalmente HTML/JavaScript
        // El formulario enviará datos via Fetch API a CrudPage
    }
}