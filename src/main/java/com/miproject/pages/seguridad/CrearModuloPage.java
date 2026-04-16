package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Modulo;
import com.miproject.dao.ModuloDAO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import java.util.List;
import java.util.Arrays;

public class CrearModuloPage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private String nombreModulo;
    private String tipo = "principal";
    private Modulo menuPadreSeleccionado;
    private Integer estado = 1;
    private List<Modulo> menusPrincipales;
    private String mensajeError;

    private DropDownChoice<Modulo> padreSelector;

    public CrearModuloPage() {
        super();

        menusPrincipales = moduloDAO.obtenerMenusPrincipales();

        add(new Label("titulo", "Crear Nuevo Módulo"));

        Form<Void> form = new Form<>("formulario");

        // Campo nombre
        TextField<String> nombreField = new TextField<>("nombre", new PropertyModel<>(this, "nombreModulo"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(3, 20));
        // Agregar el límite al HTML directamente
        nombreField.add(new org.apache.wicket.AttributeModifier("maxlength", "20"));
        form.add(nombreField);

        // ===== PRIMERO DECLARAR EL SELECTOR DE PADRE =====
        List<Modulo> menusPrincipales = moduloDAO.obtenerMenusPrincipales();

        padreSelector = new DropDownChoice<Modulo>("menuPadre",
                new PropertyModel<Modulo>(this, "menuPadreSeleccionado"),
                menusPrincipales,
                new org.apache.wicket.markup.html.form.ChoiceRenderer<Modulo>("strNombreModulo", "id")) {
            @Override
            public boolean isVisible() {
                return "submenu".equals(tipo);
            }
        };
        padreSelector.setNullValid(true);
        padreSelector.setOutputMarkupId(true);
        padreSelector.setOutputMarkupPlaceholderTag(true);

        // ===== TIPO DE MÓDULO =====
        RadioGroup<String> tipoGroup = new RadioGroup<>("tipo", new PropertyModel<>(this, "tipo"));
        tipoGroup.add(new Radio<>("principal", new Model<>("principal")));
        tipoGroup.add(new Radio<>("submenu", new Model<>("submenu")));
        tipoGroup.setRequired(true);

        // Detectar cambios en el tipo para mostrar/ocultar el selector de padre
        tipoGroup.add(new org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(padreSelector);
            }
        });

        form.add(tipoGroup);

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

        // Botón guardar
        AjaxButton btnGuardar = new AjaxButton("btnGuardar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // Validaciones
                if (nombreModulo == null || nombreModulo.trim().isEmpty()) {
                    mensajeError = "El nombre del módulo es obligatorio.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                if (moduloDAO.existeNombre(nombreModulo)) {
                    mensajeError = "Ya existe un módulo con ese nombre.";
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

                Modulo nuevoModulo = new Modulo();
                nuevoModulo.setStrNombreModulo(nombreModulo.trim());
                nuevoModulo.setIdMenuPadre(padre);
                nuevoModulo.setIdEstado(estadoValor);

                if (moduloDAO.crear(nuevoModulo)) {
                    if (padre == 0) {
                        // El mensaje se guarda en la sesión para que sobreviva al redireccionamiento
                        getSession().success("¡Menú Padre '" + nombreModulo + "' creado con éxito!");
                    } else {
                        getSession().info("Submenú creado correctamente.");
                    }

                    setResponsePage(ModuloPage.class);
                } else {
                    mensajeError = "Error al guardar en la base de datos.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                }
            }
        };
        form.add(btnGuardar);

        org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> btnCancelar = new org.apache.wicket.markup.html.link.BookmarkablePageLink<>(
                "cancelar", ModuloPage.class);
        form.add(btnCancelar);

        add(form);
    }

    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Gestión de Módulos", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Crear", CrearModuloPage.class));
        return list;
    }
}