package com.miproject;

import java.util.List;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class EliminarUsuarioPage extends BasePage {
    
    private final int idUsuario;
    
    public EliminarUsuarioPage(PageParameters parameters) {
        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Gestión Usuarios", GestionUsuariosPage.class),
            new BreadcrumbItem("Eliminar", EliminarUsuarioPage.class)
        ));
        
        // Obtener ID del usuario de los parámetros
        this.idUsuario = parameters.get("id").toInt(-1);
        
        // AÑADE ESTA LÍNEA CRÍTICA (igual que en EditarUsuarioPage):
    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
}