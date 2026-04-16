package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.components.PaginacionPanel;
import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.services.JWTService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import com.miproject.components.PaginacionPanel;

import javax.servlet.http.Cookie;
import java.util.List;

public class PerfilPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private PermisoDAO permisoDAO = new PermisoDAO();
    private List<Perfil> perfiles;
    private int paginaActual = 1;
    private int tamanoPagina = 5;
    private String filtroBusqueda = "";
    private int totalResultados;

    private PaginacionPanel paginacionPanel;

    private static final int ID_MODULO_PERFIL = 1;
    private boolean puedeAgregar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeConsultar;

    public PerfilPage() {
        super();

        add(new FeedbackPanel("feedback"));

        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        System.out.println("DEBUG CRÍTICO: El Perfil ID que viene del Token es: " + idPerfilUsuario);

        Perfil perfilActual = perfilDAO.obtenerPorId(idPerfilUsuario);
        boolean esAdmin = (perfilActual != null && perfilActual.isBitAdministrador());

        if (esAdmin) {
            System.out.println("DEBUG: Entrando como ADMINISTRADOR");
            puedeAgregar = puedeEditar = puedeEliminar = puedeConsultar = true;
        } else {
            System.out.println("DEBUG: Entrando como USUARIO REGULAR. Buscando modulo 1...");
            PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, ID_MODULO_PERFIL);

            if (permisos != null) {
                System.out.println("DEBUG ÉXITO: Permisos encontrados -> C:" + permisos.isBitConsulta() + " A:"
                        + permisos.isBitAgregar());
                puedeAgregar = permisos.isBitAgregar();
                puedeEditar = permisos.isBitEditar();
                puedeEliminar = permisos.isBitEliminar();
                puedeConsultar = permisos.isBitConsulta();
            } else {
                System.out.println("DEBUG ERROR: El DAO devolvió NULL para Perfil: " + idPerfilUsuario + " y Modulo: "
                        + ID_MODULO_PERFIL);
                error("Acceso denegado: No hay permisos configurados en la base de datos.");
                puedeAgregar = puedeEditar = puedeEliminar = puedeConsultar = false;
            }
        }

        if (puedeConsultar) {
            cargarDatos();
        }

        add(new Label("titulo", "Gestión de Perfiles"));

        Label mensajeSinPermiso = new Label("mensajeSinPermiso", "No tienes permiso para ver esta página.");
        mensajeSinPermiso.setVisible(!puedeConsultar);
        add(mensajeSinPermiso);

        BookmarkablePageLink<Void> nuevoLink = new BookmarkablePageLink<>("nuevoPerfil", CrearPerfilPage.class);
        nuevoLink.setVisible(puedeAgregar && puedeConsultar);
        add(nuevoLink);

        WebMarkupContainer tablaContainer = new WebMarkupContainer("tablaContainer");
        tablaContainer.setOutputMarkupId(true);
        tablaContainer.setVisible(puedeConsultar);
        add(tablaContainer);

        Form<Void> busquedaForm = new Form<>("busquedaForm");
        busquedaForm.setVisible(puedeConsultar);
        TextField<String> busquedaField = new TextField<>("busqueda", new Model<>(""));
        busquedaField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filtroBusqueda = busquedaField.getModelObject();
                paginaActual = 1;
                cargarDatos();
                target.add(tablaContainer);
            }
        });
        busquedaForm.add(busquedaField);
        add(busquedaForm);

        // 3. TABLA DE PERFILES
        ListView<Perfil> perfilList = new ListView<Perfil>("perfilList", new PropertyModel<>(this, "perfiles")) {
            @Override
            protected void populateItem(ListItem<Perfil> item) {
                Perfil perfil = item.getModelObject();

                item.add(new Label("nombrePerfil", perfil.getStrNombrePerfil()));

                String rol = perfil.isBitAdministrador() ? "ADMINISTRADOR" : "USUARIO";
                item.add(new Label("rol", rol));

                PageParameters params = new PageParameters();
                params.add("id", perfil.getId());

                BookmarkablePageLink<Void> editarLink = new BookmarkablePageLink<>("editarPerfil",
                        EditarPerfilPage.class, params);
                editarLink.setVisible(puedeEditar);
                item.add(editarLink);

                BookmarkablePageLink<Void> eliminarLink = new BookmarkablePageLink<>("eliminarPerfil",
                        EliminarPerfilPage.class, params);
                eliminarLink.setVisible(puedeEliminar);
                item.add(eliminarLink);
            }
        };
        tablaContainer.add(perfilList);

        paginacionPanel = new PaginacionPanel("paginacionContainer", paginaActual, totalResultados, tamanoPagina) {
            @Override
            public void onPageChange(int nuevaPagina, AjaxRequestTarget target) {
                paginaActual = nuevaPagina;
                cargarDatos();
                target.add(tablaContainer);
            }
        };
        paginacionPanel.setOutputMarkupId(true);
        paginacionPanel.setVisible(puedeConsultar);
        add(paginacionPanel);
    }

    private void cargarDatos() {
        perfiles = perfilDAO.buscarConFiltros(filtroBusqueda, paginaActual, tamanoPagina);
        totalResultados = perfilDAO.contarConFiltros(filtroBusqueda);
    }

    private int obtenerIdPerfilDesdeToken() {
        Cookie[] cookies = ((javax.servlet.http.HttpServletRequest) getRequest().getContainerRequest()).getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    Integer idPerfil = JWTService.getPerfilIdFromToken(cookie.getValue());
                    return idPerfil != null ? idPerfil : 1;
                }
            }
        }
        return 1;
    }

    @Override
    protected List<BasePage.BreadcrumbItem> getBreadcrumbs() {
        List<BasePage.BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BasePage.BreadcrumbItem("Seguridad", PerfilPage.class));
        list.add(new BasePage.BreadcrumbItem("Perfil", PerfilPage.class));
        return list;
    }
}