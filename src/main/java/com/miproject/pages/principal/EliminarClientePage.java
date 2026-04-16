package com.miproject.pages.principal;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.dao.ClienteDAO;
import com.miproject.models.Cliente;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

public class EliminarClientePage extends BasePage {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idMenuOrigen;
    private String nombreMenuActual;
    private Cliente clienteAEliminar;

    private String menuPadre;
    private String menuHijo;

    public EliminarClientePage(PageParameters parameters) {
        super();

        // 1. OBTENER PARÁMETROS DESDE LA URL
        idMenuOrigen = parameters.get("origen").toInt(1);
        int idCliente = parameters.get("id").toInt(-1);

        // Identificar en qué menú estamos
        switch (idMenuOrigen) {
            case 1: nombreMenuActual = "Principal 1.1"; break;
            case 2: nombreMenuActual = "Principal 1.2"; break;
            case 3: nombreMenuActual = "Principal 2.1"; break;
            case 4: nombreMenuActual = "Principal 2.2"; break;
            default: nombreMenuActual = "Principal 1.1"; break;
        }

        idMenuOrigen = parameters.get("origen").toInt(8);

        switch (idMenuOrigen) {
            case 8: 
                menuPadre = "Principal 1"; menuHijo = "Principal 1.1"; break;
            case 9: 
                menuPadre = "Principal 1"; menuHijo = "Principal 1.2"; break;
            case 10: 
                menuPadre = "Principal 2"; menuHijo = "Principal 2.1"; break;
            case 11: 
                menuPadre = "Principal 2"; menuHijo = "Principal 2.2"; break;
            default: 
                menuPadre = "Módulo"; menuHijo = "Clientes"; break;
        }

        if (idCliente != -1) {
            clienteAEliminar = clienteDAO.obtenerPorId(idCliente);
        }

        if (clienteAEliminar == null) {
            getSession().error("El cliente que intentas eliminar no existe o ya fue borrado.");
            PageParameters params = new PageParameters().add("origen", idMenuOrigen);
            setResponsePage(ClientePage.class, params);
            return; 
        }

        add(new Label("titulo", "Eliminar Cliente (" + nombreMenuActual + ")"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        add(new Label("lblCodigo", clienteAEliminar.getStrCodigoCliente()));
        add(new Label("lblNombre", clienteAEliminar.getStrNombre()));
        add(new Label("lblEmpresa", clienteAEliminar.getStrEmpresa()));

        Form<Void> form = new Form<Void>("formEliminar") {
            @Override
            protected void onSubmit() {
                if (clienteDAO.eliminar(clienteAEliminar.getId())) {
                    getSession().success("¡El cliente '" + clienteAEliminar.getStrNombre() + "' fue eliminado con éxito!");
                    
                    PageParameters params = new PageParameters();
                    params.add("origen", idMenuOrigen);
                    setResponsePage(ClientePage.class, params);
                } else {
                    error("Hubo un problema al intentar eliminar el cliente de la base de datos.");
                }
            }
        };

        add(form);

        form.add(new Link<Void>("btnCancelar") {
            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.add("origen", idMenuOrigen);
                setResponsePage(ClientePage.class, params);
            }
        });
    }

@Override
protected List<BreadcrumbItem> getBreadcrumbs() {
    List<BreadcrumbItem> list = super.getBreadcrumbs();
    list.add(new BreadcrumbItem(menuPadre, HomePage.class));
    list.add(new BreadcrumbItem(menuHijo, ClientePage.class));
    list.add(new BreadcrumbItem("Eliminar Cliente", EliminarClientePage.class));
    return list;
}
}
