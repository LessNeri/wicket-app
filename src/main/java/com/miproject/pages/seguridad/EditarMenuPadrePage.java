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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import java.util.List;
import java.util.Arrays;

public class EditarMenuPadrePage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private Modulo moduloActual;
    private String nombreModulo;
    private Integer estado;
    private String mensajeError;

    public EditarMenuPadrePage(PageParameters parameters) {
        super();

        int idModulo = parameters.get("id").toInt();
        moduloActual = moduloDAO.obtenerPorId(idModulo);

        // Seguridad: Verificar que exista y que realmente sea un menú padre
        if (moduloActual == null || moduloActual.getIdMenuPadre() != 0) {
            error("Menú Principal no encontrado o inválido");
            setResponsePage(ModuloPage.class);
            return;
        }

        nombreModulo = moduloActual.getStrNombreModulo();
        estado = moduloActual.getIdEstado();

        add(new Label("titulo", "Editar Menú Principal: " + nombreModulo));

        Form<Void> form = new Form<>("formulario");

        TextField<String> nombreField = new TextField<>("nombre", new PropertyModel<>(this, "nombreModulo"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(3, 20));
        form.add(nombreField);

        DropDownChoice<Integer> estadoSelector = new DropDownChoice<>("estado",
                new PropertyModel<>(this, "estado"), Arrays.asList(1, 0));
        estadoSelector.setChoiceRenderer(new org.apache.wicket.markup.html.form.IChoiceRenderer<Integer>() {
            @Override
            public Object getDisplayValue(Integer value) { return value == 1 ? "Activo" : "Inactivo"; }
            @Override
            public String getIdValue(Integer value, int index) { return String.valueOf(value); }
        });
        estadoSelector.setRequired(true);
        form.add(estadoSelector);

        Label errorLabel = new Label("mensajeError", new PropertyModel<>(this, "mensajeError"));
        errorLabel.setOutputMarkupId(true);
        errorLabel.setVisible(false);
        form.add(errorLabel);

        AjaxButton btnActualizar = new AjaxButton("btnActualizar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (nombreModulo == null || nombreModulo.trim().isEmpty()) {
                    mensajeError = "El nombre es obligatorio.";
                    errorLabel.setVisible(true); target.add(errorLabel); return;
                }

                if (moduloDAO.existeNombreExcepto(nombreModulo, moduloActual.getId())) {
                    mensajeError = "Ya existe otro módulo con ese nombre.";
                    errorLabel.setVisible(true); target.add(errorLabel); return;
                }

                moduloActual.setStrNombreModulo(nombreModulo.trim());
                moduloActual.setIdEstado(estado != null ? estado : 1);

                if (moduloDAO.actualizar(moduloActual)) {
                    getSession().success("¡Menú Principal '" + nombreModulo + "' actualizado!");
                    setResponsePage(ModuloPage.class);
                } else {
                    mensajeError = "Error al actualizar en base de datos.";
                    errorLabel.setVisible(true); target.add(errorLabel);
                }
            }
        };
        form.add(btnActualizar);

        form.add(new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("cancelar", ModuloPage.class));
        add(form);
    }

    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Gestión de Módulos", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Editar Menú Principal", EditarMenuPadrePage.class));
        return list;
    }
}