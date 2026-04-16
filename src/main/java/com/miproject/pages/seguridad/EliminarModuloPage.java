package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Modulo;
import com.miproject.dao.ModuloDAO;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import java.util.List;

public class EliminarModuloPage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private Modulo modulo;

    public EliminarModuloPage(PageParameters parameters) {
        super();

        int idModulo = parameters.get("id").toInt();
        modulo = moduloDAO.obtenerPorId(idModulo);

        if (modulo == null) {
            error("Módulo no encontrado");
            setResponsePage(ModuloPage.class);
            return;
        }

        add(new org.apache.wicket.markup.html.panel.FeedbackPanel("feedback"));

        // Verificar si tiene submenús asociados
        boolean tieneSubmenus = moduloDAO.tieneSubmenus(idModulo);
        String mensajeAdvertencia = "";
        if (tieneSubmenus) {
            mensajeAdvertencia = "Este módulo tiene submenús asociados. Si lo eliminas, también se eliminarán sus submenús.";
        }

        add(new Label("titulo", "Eliminar Módulo"));
        add(new Label("nombreModulo", modulo.getStrNombreModulo()));
        
        // Mostrar estado
        String estadoTexto = modulo.getIdEstado() == 1 ? "Activo" : "Inactivo";
        add(new Label("estadoModulo", estadoTexto));
        
        // Mostrar tipo
        String tipoTexto = modulo.getIdMenuPadre() == 0 ? "Menú Principal" : "Submenú";
        add(new Label("tipoModulo", tipoTexto));
        
        // Mostrar menú padre si es submenú
        String padreTexto = "No aplica";
        if (modulo.getIdMenuPadre() != 0) {
            Modulo padre = moduloDAO.obtenerPorId(modulo.getIdMenuPadre());
            if (padre != null) {
                padreTexto = padre.getStrNombreModulo();
            }
        }
        add(new Label("padreModulo", padreTexto));
        
        // Mensaje de advertencia
        Label warningLabel = new Label("mensajeAdvertencia", mensajeAdvertencia);
        warningLabel.setVisible(tieneSubmenus);
        add(warningLabel);

        // Botón confirmar
        add(new Link<Void>("confirmar") {
            @Override
            public void onClick() {
                String nombreEliminado = modulo.getStrNombreModulo();
                boolean exito = moduloDAO.eliminar(modulo.getId());
                
                if (exito) {
                    getSession().success("El módulo '" + nombreEliminado + "' ha sido eliminado.");
                    setResponsePage(ModuloPage.class);
                } else {
                    error("Error al eliminar el módulo de la base de datos.");
                }
            }
        });

        // Botón cancelar
        add(new Link<Void>("cancelar") {
            @Override
            public void onClick() {
                setResponsePage(ModuloPage.class);
            }
        });
    }
    
    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Gestión de Módulos", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Eliminar Módulo", EliminarModuloPage.class));
        return list;
    }
}