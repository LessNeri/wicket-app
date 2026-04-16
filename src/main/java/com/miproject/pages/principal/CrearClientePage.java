package com.miproject.pages.principal;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.dao.ClienteDAO;
import com.miproject.models.Cliente;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

import java.util.List;

public class CrearClientePage extends BasePage {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idMenuOrigen;
    private String nombreMenuActual;

    private String menuPadre;
    private String menuHijo;

    public CrearClientePage(PageParameters parameters) {
        super();

    idMenuOrigen = parameters.get("origen").toInt(5); 

    switch (idMenuOrigen) {
        case 5: 
            nombreMenuActual = "Principal 1.1";
            menuPadre = "Principal 1"; 
            menuHijo = "Principal 1.1"; 
            break;
        case 6: 
            nombreMenuActual = "Principal 1.2";
            menuPadre = "Principal 1"; 
            menuHijo = "Principal 1.2"; 
            break;
        case 7: 
            nombreMenuActual = "Principal 2.1";
            menuPadre = "Principal 2"; 
            menuHijo = "Principal 2.1"; 
            break;
        case 8: 
            nombreMenuActual = "Principal 2.2";
            menuPadre = "Principal 2"; 
            menuHijo = "Principal 2.2"; 
            break;
        default: 
            nombreMenuActual = "General";
            menuPadre = "Módulo"; 
            menuHijo = "Clientes"; 
            break;
    }

        add(new Label("titulo", "Registrar Nuevo Cliente (" + nombreMenuActual + ")"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        Cliente nuevoCliente = new Cliente();
        Form<Cliente> form = new Form<Cliente>("formCrear", new CompoundPropertyModel<>(nuevoCliente)) {
            @Override
            protected void onSubmit() {
                Cliente cliente = getModelObject();

                if (clienteDAO.existeCodigo(cliente.getStrCodigoCliente(), 0)) {
                    error("El código '" + cliente.getStrCodigoCliente() + "' ya existe en el sistema.");
                    return;
                }

                cliente.setIdMenuOrigen(idMenuOrigen); 
                cliente.setIdEstado(1);

                // Guardar
                if (clienteDAO.crear(cliente)) {
                    getSession().success("¡Cliente registrado correctamente!");
                    
                    PageParameters params = new PageParameters();
                    params.add("origen", idMenuOrigen);
                    setResponsePage(ClientePage.class, params);
                } else {
                    error("Hubo un problema al guardar el cliente en la base de datos.");
                }
            }
        };


        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
        String jsSoloLetras = "this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]/g, '');";
        String jsSoloNumeros = "this.value = this.value.replace(/[^0-9]/g, '');";
        String jsCodigoAvanzado = "this.value = this.value.replace(/[^a-zA-Z0-9-]/g, '').toUpperCase();";

        form.add(new TextField<String>("strCodigoCliente") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "10");
                tag.put("oninput", jsCodigoAvanzado);
            }
        }.setRequired(true).setLabel(Model.of("Código del Cliente"))
         .add(StringValidator.maximumLength(10)));

        // B. NOMBRE (Solo letras y espacios)
        form.add(new TextField<String>("strNombre") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "50");
                tag.put("pattern", regexLetras);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true).setLabel(Model.of("Nombre del Cliente"))
         .add(StringValidator.maximumLength(50))
         .add(new PatternValidator(regexLetras)));

        form.add(new TextField<String>("strTelefono") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "10");
                tag.put("pattern", "[0-9]{10}");
                tag.put("oninput", jsSoloNumeros); // Bloquea letras en tiempo real
            }
        }.setRequired(true).setLabel(Model.of("Teléfono"))
         .add(new PatternValidator("^[0-9]{10}$")));

        form.add(new TextField<String>("strEmpresa") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "100");
            }
        }.setRequired(true).setLabel(Model.of("Empresa"))
         .add(StringValidator.maximumLength(100)));

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
protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
    List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
    list.add(new BasePage.BreadcrumbItem(menuPadre, ClientePage.class)); 
    list.add(new BasePage.BreadcrumbItem(menuHijo, ClientePage.class));
    list.add(new BasePage.BreadcrumbItem("Crear Cliente", CrearClientePage.class));
    
    return list;
}
}