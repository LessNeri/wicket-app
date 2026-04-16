package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Modulo;
import com.miproject.dao.ModuloDAO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import java.util.List;

public class EliminarMenuPadrePage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private Modulo modulo;
    private boolean entiendoConsecuencias = false; // Checkbox de doble confirmación
    private boolean tieneSubmenus;

    public EliminarMenuPadrePage(PageParameters parameters) {
        super();

        int idModulo = parameters.get("id").toInt();
        modulo = moduloDAO.obtenerPorId(idModulo);

        if (modulo == null || modulo.getIdMenuPadre() != 0) {
            error("Menú Principal no encontrado o es un submenú.");
            setResponsePage(ModuloPage.class);
            return;
        }

        tieneSubmenus = moduloDAO.tieneSubmenus(idModulo);

        add(new Label("nombreModulo", modulo.getStrNombreModulo()));
        
        Form<Void> form = new Form<>("formEliminar");
        add(form);

        // Contenedor de advertencia severa (solo visible si tiene submenús)
        WebMarkupContainer warningContainer = new WebMarkupContainer("warningContainer");
        warningContainer.setVisible(tieneSubmenus);
        form.add(warningContainer);

        // Checkbox de confirmación dentro de la advertencia
        CheckBox chkConfirmacion = new CheckBox("chkConfirmacion", new PropertyModel<>(this, "entiendoConsecuencias"));
        warningContainer.add(chkConfirmacion);

        Label lblErrorConfirmacion = new Label("lblErrorConfirmacion", "DEBES MARCAR LA CASILLA PARA CONFIRMAR LA ELIMINACIÓN EN CASCADA.");
        lblErrorConfirmacion.setOutputMarkupId(true);
        lblErrorConfirmacion.setVisible(false);
        form.add(lblErrorConfirmacion);

        AjaxButton btnEliminar = new AjaxButton("btnEliminar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // Validación de la doble confirmación
                if (tieneSubmenus && !entiendoConsecuencias) {
                    lblErrorConfirmacion.setVisible(true);
                    target.add(lblErrorConfirmacion);
                    return;
                }

                String nombreEliminado = modulo.getStrNombreModulo();
                boolean exito = moduloDAO.eliminar(modulo.getId()); // Asumo que tu DAO borra en cascada
                
                if (exito) {
                    getSession().success("El Menú Principal '" + nombreEliminado + "' y sus dependencias fueron eliminados.");
                    setResponsePage(ModuloPage.class);
                } else {
                    error("Error grave al intentar eliminar el menú principal de la base de datos.");
                }
            }
        };
        form.add(btnEliminar);

        form.add(new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("cancelar", ModuloPage.class));
    }

    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Gestión de Módulos", ModuloPage.class));
        list.add(new BasePage.BreadcrumbItem("Eliminar Menú Principal", EliminarMenuPadrePage.class));
        return list;
    }
}