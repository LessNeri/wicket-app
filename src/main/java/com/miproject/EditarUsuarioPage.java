package com.miproject;

import java.util.List;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

public class EditarUsuarioPage extends BasePage {
    
    private final int idUsuario;
    
    public EditarUsuarioPage(PageParameters parameters) {
        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Gestión Usuarios", GestionUsuariosPage.class),
            new BreadcrumbItem("Editar", EditarUsuarioPage.class)
        ));
        
        // Obtener ID del usuario de los parámetros
        this.idUsuario = parameters.get("id").toInt(-1);
        
        // AÑADE ESTA LÍNEA CRÍTICA:
        add(new Label("idUsuario", String.valueOf(idUsuario)));
    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
}