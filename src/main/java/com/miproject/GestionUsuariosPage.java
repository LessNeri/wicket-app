package com.miproject;

import java.util.List;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class GestionUsuariosPage extends BasePage {
    
    public GestionUsuariosPage() {
        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Gestión Usuarios", GestionUsuariosPage.class)
        ));
        
        // Link para registrar nuevo usuario
        add(new BookmarkablePageLink<>("linkRegistrar", RegistrarUsuarioPage.class));
        
        // La tabla se carga con JavaScript/Fetch API desde el HTML
        // Los botones "Editar" y "Eliminar" ahora usarán BookmarkablePageLink en JavaScript
    }
}