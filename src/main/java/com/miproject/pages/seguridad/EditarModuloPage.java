package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Modulo;
import com.miproject.dao.ModuloDAO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import java.util.List;
import java.util.Arrays;

public class EditarModuloPage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private Modulo moduloActual;
    private String nombreModulo;
    private String tipo;
    private Modulo menuPadreSeleccionado;
    private Integer estado;
    private List<Modulo> menusPrincipales;
    private String mensajeError;
    private DropDownChoice<Modulo> padreSelector;

    public EditarModuloPage(PageParameters parameters) {
        super();

        int idModulo = parameters.get("id").toInt();
        moduloActual = moduloDAO.obtenerPorId(idModulo);

        if (moduloActual == null) {
            error("Módulo no encontrado");
            setResponsePage(ModuloPage.class);
            return;
        }

        // Cargar datos actuales
        nombreModulo = moduloActual.getStrNombreModulo();
        estado = moduloActual.getIdEstado();
        
        // Determinar tipo (principal o submenú)
        if (moduloActual.getIdMenuPadre() == 0) {
            tipo = "principal";
            menuPadreSeleccionado = null;
        } else {
            tipo = "submenu";
            // Buscar el módulo padre
            for (Modulo m : moduloDAO.obtenerMenusPrincipales()) {
                if (m.getId() == moduloActual.getIdMenuPadre()) {
                    menuPadreSeleccionado = m;
                    break;
                }
            }
        }

        // Cargar menús principales disponibles
        menusPrincipales = moduloDAO.obtenerMenusPrincipales();
        // Remover el módulo actual de la lista de padres (no puede ser padre de sí mismo)
        menusPrincipales.removeIf(m -> m.getId() == idModulo);

        add(new Label("titulo", "Editar Módulo: " + nombreModulo));

        Form<Void> form = new Form<>("formulario");

        // Campo nombre
        TextField<String> nombreField = new TextField<>("nombre", new PropertyModel<>(this, "nombreModulo"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(3, 20));
        form.add(nombreField);

        // ===== DECLARAR SELECTOR DE PADRE PRIMERO =====
padreSelector = new DropDownChoice<Modulo>("menuPadre",
        new PropertyModel<Modulo>(this, "menuPadreSeleccionado"),
        menusPrincipales,
        new org.apache.wicket.markup.html.form.ChoiceRenderer<Modulo>("strNombreModulo", "id")) { // <- ¡AQUÍ ESTÁ LA MAGIA!
    @Override
    public boolean isVisible() {
        return "submenu".equals(tipo);
    }
};
padreSelector.setNullValid(true);
padreSelector.setOutputMarkupId(true);

        // ===== TIPO DE MÓDULO =====
        RadioGroup<String> tipoGroup = new RadioGroup<>("tipo", new PropertyModel<>(this, "tipo"));
        tipoGroup.add(new Radio<>("principal", new Model<>("principal")));
        tipoGroup.add(new Radio<>("submenu", new Model<>("submenu")));
        tipoGroup.setRequired(true);
        
        // Detectar cambios en el tipo
        tipoGroup.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(padreSelector);
            }
        });
        form.add(tipoGroup);
        
        // Agregar selector de padre
        form.add(padreSelector);

        // Estado
        DropDownChoice<Integer> estadoSelector = new DropDownChoice<>("estado",
                new PropertyModel<>(this, "estado"),
                Arrays.asList(1, 0));
        estadoSelector.setChoiceRenderer(new org.apache.wicket.markup.html.form.IChoiceRenderer<Integer>() {
            @Override
            public Object getDisplayValue(Integer value) {
                return value == 1 ? "Activo" : "Inactivo";
            }
            @Override
            public String getIdValue(Integer value, int index) {
                return String.valueOf(value);
            }
        });
        estadoSelector.setRequired(true);
        form.add(estadoSelector);

        // Mensaje de error
        Label errorLabel = new Label("mensajeError", new PropertyModel<>(this, "mensajeError"));
        errorLabel.setOutputMarkupId(true);
        errorLabel.setVisible(false);
        form.add(errorLabel);

        // Botón actualizar
        AjaxButton btnActualizar = new AjaxButton("btnActualizar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // Validaciones
                if (nombreModulo == null || nombreModulo.trim().isEmpty()) {
                    mensajeError = "El nombre del módulo es obligatorio.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                // Verificar nombre duplicado (excluyendo el actual)
                if (moduloDAO.existeNombreExcepto(nombreModulo, moduloActual.getId())) {
                    mensajeError = "Ya existe otro módulo con ese nombre.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                if ("submenu".equals(tipo) && menuPadreSeleccionado == null) {
                    mensajeError = "Debe seleccionar un menú padre para el submenú.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                int padre = "principal".equals(tipo) ? 0 : menuPadreSeleccionado.getId();
                int estadoValor = estado != null ? estado : 1;

                moduloActual.setStrNombreModulo(nombreModulo.trim());
                moduloActual.setIdMenuPadre(padre);
                moduloActual.setIdEstado(estadoValor);

                boolean exito = moduloDAO.actualizar(moduloActual);
                if (exito) {
                    getSession().success("¡Módulo '" + nombreModulo + "' actualizado correctamente!");
                    setResponsePage(ModuloPage.class);
                } else {
                    mensajeError = "Error al actualizar el módulo.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                }
            }
        };
        form.add(btnActualizar);

org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> btnCancelar = 
    new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("cancelar", ModuloPage.class);
form.add(btnCancelar);

        add(form);
    }

    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Gestión de Módulos", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Editar", EditarModuloPage.class));
        return list;
    }
}