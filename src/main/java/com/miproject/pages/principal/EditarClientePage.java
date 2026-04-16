package com.miproject.pages.principal;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.dao.ClienteDAO;
import com.miproject.models.Cliente;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

import java.util.Arrays;
import java.util.List;

public class EditarClientePage extends BasePage {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idMenuOrigen;
    private String nombreMenuActual;
    private Cliente clienteAEditar;

    private String menuPadre;
    private String menuHijo;

    public EditarClientePage(PageParameters parameters) {
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

        // 2. BUSCAR EL CLIENTE EN BASE DE DATOS
        if (idCliente != -1) {
            clienteAEditar = clienteDAO.obtenerPorId(idCliente);
        }

        // Seguridad: Si alteran la URL con un ID falso, los regresamos a la tabla
        if (clienteAEditar == null) {
            getSession().error("El cliente que intentas editar no existe.");
            PageParameters params = new PageParameters().add("origen", idMenuOrigen);
            setResponsePage(ClientePage.class, params);
            return; // Detiene la ejecución para no causar NullPointerException
        }

        add(new Label("titulo", "Editar Cliente (" + nombreMenuActual + ")"));

        // Panel de errores
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        // 3. FORMULARIO PRINCIPAL (Ya cargado con los datos de clienteAEditar)
        Form<Cliente> form = new Form<Cliente>("formEditar", new CompoundPropertyModel<>(clienteAEditar)) {
            @Override
            protected void onSubmit() {
                Cliente cliente = getModelObject();

                // Validar que el código no lo tenga *OTRO* cliente distinto a este
                if (clienteDAO.existeCodigo(cliente.getStrCodigoCliente(), cliente.getId())) {
                    error("El código '" + cliente.getStrCodigoCliente() + "' ya está siendo usado por otro cliente.");
                    return;
                }

                // Guardar cambios usando el método actualizar (UPDATE)
                if (clienteDAO.actualizar(cliente)) {
                    getSession().success("¡Cliente actualizado correctamente!");
                    
                    PageParameters params = new PageParameters();
                    params.add("origen", idMenuOrigen);
                    setResponsePage(ClientePage.class, params);
                } else {
                    error("Hubo un problema al actualizar el cliente en la base de datos.");
                }
            }
        };

        // --- 4. CAMPOS Y VALIDACIONES FRONTEND/BACKEND ---
        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
        String jsSoloLetras = "this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]/g, '');";
        String jsSoloNumeros = "this.value = this.value.replace(/[^0-9]/g, '');";
        String jsCodigoAvanzado = "this.value = this.value.replace(/[^a-zA-Z0-9-]/g, '').toUpperCase();";

        // A. CÓDIGO
        form.add(new TextField<String>("strCodigoCliente") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "10");
                tag.put("oninput", jsCodigoAvanzado);
            }
        }.setRequired(true).setLabel(Model.of("Código del Cliente"))
         .add(StringValidator.maximumLength(10)));

        // B. NOMBRE
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

        // C. TELÉFONO
        form.add(new TextField<String>("strTelefono") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "10");
                tag.put("pattern", "[0-9]{10}");
                tag.put("oninput", jsSoloNumeros);
            }
        }.setRequired(true).setLabel(Model.of("Teléfono"))
         .add(new PatternValidator("^[0-9]{10}$")));

        // D. EMPRESA
        form.add(new TextField<String>("strEmpresa") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "100");
            }
        }.setRequired(true).setLabel(Model.of("Empresa"))
         .add(StringValidator.maximumLength(100)));

        // E. ESTADO (Activo / Inactivo) - ¡EXCLUSIVO DE EDICIÓN!
        form.add(new DropDownChoice<Integer>("idEstado", 
            Arrays.asList(1, 0),
            new IChoiceRenderer<Integer>() {
                @Override
                public Object getDisplayValue(Integer object) { return (object == 1) ? "Activo" : "Inactivo"; }
                @Override
                public String getIdValue(Integer object, int index) { return object.toString(); }
                @Override
                public Integer getObject(String id, IModel<? extends List<? extends Integer>> choices) { return Integer.valueOf(id); }
            }
        ).setRequired(true).setLabel(Model.of("Estado del Cliente")));

        add(form);

        // 5. BOTÓN CANCELAR
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
    list.add(new BreadcrumbItem("Editar Cliente", EditarClientePage.class));
    return list;
}
}