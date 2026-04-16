package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Modulo;
import com.miproject.models.Menu;
import com.miproject.models.PermisoPerfil;
import com.miproject.dao.ModuloDAO;
import com.miproject.dao.MenuDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.services.JWTService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import com.miproject.models.Perfil;
import com.miproject.dao.PerfilDAO;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuloPage extends BasePage {

    private ModuloDAO moduloDAO = new ModuloDAO();
    private MenuDAO menuDAO = new MenuDAO();
    private PermisoDAO permisoDAO = new PermisoDAO();
    
    private List<Modulo> modulos;
    private List<Modulo> padres;
    
    private Map<Integer, String> menuMap = new HashMap<>();
    private int paginaActual = 1;
    private int tamanoPagina = 5;
    private String filtroBusqueda = "";
    private int totalResultados;
    
    private static final int ID_MODULO_MODULO = 2;
    private boolean puedeAgregar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeConsultar;

    private int paginaActualPadres = 1;
    private int tamanoPaginaPadres = 5;
    private String filtroBusquedaPadres = "";
    private int totalResultadosPadres;

    public ModuloPage() {
        super();

        cargarMenuMap();
        cargarDatos();
        
        int idPerfilUsuario = obtenerIdPerfilDesdeToken();

        Perfil perfilActual = new PerfilDAO().obtenerPorId(idPerfilUsuario);
        boolean esAdmin = (perfilActual != null && perfilActual.isBitAdministrador());

        if (esAdmin) {
            puedeAgregar = true;
            puedeEditar = true;
            puedeEliminar = true;
            puedeConsultar = true;
        } else {
            PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, ID_MODULO_MODULO);

            if (permisos != null) {
                puedeAgregar = permisos.isBitAgregar();
                puedeEditar = permisos.isBitEditar();
                puedeEliminar = permisos.isBitEliminar();
                puedeConsultar = permisos.isBitConsulta();
            } else {
                puedeAgregar = puedeEditar = puedeEliminar = puedeConsultar = false;
            }
        }

        Label lblMensajeSinPermiso = new Label("mensajeSinPermiso", "No tienes permiso para ver esta página.");
        lblMensajeSinPermiso.setVisible(!puedeConsultar);
        add(lblMensajeSinPermiso);

        org.apache.wicket.markup.html.WebMarkupContainer contenidoPrincipal = new org.apache.wicket.markup.html.WebMarkupContainer("contenidoPrincipal");
        contenidoPrincipal.setVisible(puedeConsultar);
        contenidoPrincipal.setOutputMarkupId(true); 
        add(contenidoPrincipal);

        contenidoPrincipal.add(new org.apache.wicket.markup.html.panel.FeedbackPanel("feedback"));

        contenidoPrincipal.add(new Label("titulo", "Gestión de Módulos"));
        contenidoPrincipal.add(new Label("totalResultados", String.valueOf(totalResultados)));

        org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> nuevoLink = 
            new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("nuevoModulo", CrearModuloPage.class);
        nuevoLink.setVisible(puedeAgregar);
        contenidoPrincipal.add(nuevoLink);      
        
        // 1. Buscador (Queda fuera del contenedor que se actualiza)
        Form<Void> busquedaForm = new Form<>("busquedaForm");
        TextField<String> busquedaField = new TextField<>("busqueda", new Model<>(""));
        busquedaField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filtroBusqueda = busquedaField.getModelObject();
                if (filtroBusqueda == null) filtroBusqueda = "";
                paginaActual = 1;
                cargarDatos(); 
                // Actualiza solo el contenedor de la tabla
                target.add(contenidoPrincipal.get("contenedorTablaSubmenus"));
            }
        });
        busquedaForm.add(busquedaField);
        contenidoPrincipal.add(busquedaForm);

        // 2. Contenedor de la tabla Submenús
        WebMarkupContainer contenedorTablaSubmenus = new WebMarkupContainer("contenedorTablaSubmenus");
        contenedorTablaSubmenus.setOutputMarkupId(true);
        contenidoPrincipal.add(contenedorTablaSubmenus);

        contenedorTablaSubmenus.add(new ListView<Modulo>("moduloList", new PropertyModel<>(this, "modulos")) {
            @Override
            protected void populateItem(ListItem<Modulo> item) {
                Modulo modulo = item.getModelObject();

                item.add(new Label("nombreModulo", modulo.getStrNombreModulo()));

                String nombrePadre = "Menú Principal";
                if (modulo.getIdMenuPadre() > 0) {
                    Modulo padre = moduloDAO.obtenerPorId(modulo.getIdMenuPadre());
                    nombrePadre = "Submenú de: " + (padre != null ? padre.getStrNombreModulo() : "Desconocido");
                }
                item.add(new Label("menuAsignado", nombrePadre));
                
                String estadoTexto = modulo.getIdEstado() == 1 ? "Activo" : "Inactivo";
                String badgeClass = modulo.getIdEstado() == 1 ? "badge badge-activo" : "badge badge-inactivo";
                Label estadoLabel = new Label("estado", estadoTexto);
                estadoLabel.add(new org.apache.wicket.AttributeModifier("class", badgeClass));
                item.add(estadoLabel);

                PageParameters params = new PageParameters();
                params.add("id", modulo.getId());

                org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> editarLink = 
                    new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("editarModulo", EditarModuloPage.class, params);
                editarLink.setVisible(puedeEditar);
                item.add(editarLink);
                
                org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> eliminarLink = 
                    new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("eliminarModulo", EliminarModuloPage.class, params);
                eliminarLink.setVisible(puedeEliminar);
                item.add(eliminarLink);
            }
        });

        org.apache.wicket.model.IModel<String> infoModel = new org.apache.wicket.model.IModel<String>() {
            @Override
            public String getObject() {
                if (totalResultados == 0) return "No se encontraron registros";
                int desde = (paginaActual - 1) * tamanoPagina + 1;
                int hasta = Math.min(paginaActual * tamanoPagina, totalResultados);
                return "Mostrando " + desde + " a " + hasta + " de " + totalResultados + " registros";
            }
        };
        contenedorTablaSubmenus.add(new Label("infoPagina", infoModel));

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnAnterior = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnAnterior") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (paginaActual > 1) {
                    paginaActual--;
                    cargarDatos();
                    target.add(contenedorTablaSubmenus);
                }
            }
        };
        contenedorTablaSubmenus.add(btnAnterior);

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnSiguiente = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnSiguiente") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                int totalPaginas = (int) Math.ceil((double) totalResultados / tamanoPagina);
                if (paginaActual < totalPaginas) {
                    paginaActual++;
                    cargarDatos();
                    target.add(contenedorTablaSubmenus);
                }
            }
        };
        contenedorTablaSubmenus.add(btnSiguiente);


        // 1. Buscador Padres (Fuera del contenedor)
        Form<Void> busquedaFormPadres = new Form<>("busquedaFormPadres");
        TextField<String> busquedaFieldPadres = new TextField<>("busquedaPadres", new Model<>(""));
        
        busquedaFieldPadres.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filtroBusquedaPadres = busquedaFieldPadres.getModelObject();
                if (filtroBusquedaPadres == null) filtroBusquedaPadres = "";
                paginaActualPadres = 1;
                cargarDatos(); 
                // Actualiza solo el contenedor de padres
                target.add(contenidoPrincipal.get("contenedorTablaPadres")); 
            }
        });
        busquedaFormPadres.add(busquedaFieldPadres);
        contenidoPrincipal.add(busquedaFormPadres);

        // 2. Contenedor de la tabla Padres
        WebMarkupContainer contenedorTablaPadres = new WebMarkupContainer("contenedorTablaPadres");
        contenedorTablaPadres.setOutputMarkupId(true);
        contenidoPrincipal.add(contenedorTablaPadres);

        contenedorTablaPadres.add(new ListView<Modulo>("padresList", new PropertyModel<>(this, "padres")) {
            @Override
            protected void populateItem(ListItem<Modulo> item) {
                Modulo padre = item.getModelObject();

                item.add(new Label("nombrePadre", padre.getStrNombreModulo()));

                String estadoTexto = padre.getIdEstado() == 1 ? "Activo" : "Inactivo";
                String badgeClass = padre.getIdEstado() == 1 ? "badge badge-activo" : "badge badge-inactivo";
                Label estadoLabel = new Label("estadoPadre", estadoTexto);
                estadoLabel.add(new org.apache.wicket.AttributeModifier("class", badgeClass));
                item.add(estadoLabel);

                PageParameters params = new PageParameters();
                params.add("id", padre.getId());

                org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> editarLink = 
                    new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("editarPadre", EditarMenuPadrePage.class, params);
                editarLink.setVisible(puedeEditar);
                item.add(editarLink);
                
                org.apache.wicket.markup.html.link.BookmarkablePageLink<Void> eliminarLink = 
                    new org.apache.wicket.markup.html.link.BookmarkablePageLink<>("eliminarPadre", EliminarMenuPadrePage.class, params);
                eliminarLink.setVisible(puedeEliminar);
                item.add(eliminarLink);
            }
        });

        org.apache.wicket.model.IModel<String> infoModelPadres = new org.apache.wicket.model.IModel<String>() {
            @Override
            public String getObject() {
                if (totalResultadosPadres == 0) return "No se encontraron registros";
                int desde = (paginaActualPadres - 1) * tamanoPaginaPadres + 1;
                int hasta = Math.min(paginaActualPadres * tamanoPaginaPadres, totalResultadosPadres);
                return "Mostrando " + desde + " a " + hasta + " de " + totalResultadosPadres + " registros";
            }
        };
        contenedorTablaPadres.add(new Label("infoPaginaPadres", infoModelPadres));

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnAnteriorPadres = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnAnteriorPadres") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (paginaActualPadres > 1) {
                    paginaActualPadres--;
                    cargarDatos();
                    target.add(contenedorTablaPadres);
                }
            }
        };
        contenedorTablaPadres.add(btnAnteriorPadres);

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnSiguientePadres = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnSiguientePadres") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                int totalPaginas = (int) Math.ceil((double) totalResultadosPadres / tamanoPaginaPadres);
                if (paginaActualPadres < totalPaginas) {
                    paginaActualPadres++;
                    cargarDatos();
                    target.add(contenedorTablaPadres);
                }
            }
        };
        contenedorTablaPadres.add(btnSiguientePadres);
    }

    private void cargarMenuMap() {
        List<Menu> menus = menuDAO.listarTodos();
        for (Menu menu : menus) {
            menuMap.put(menu.getIdModulo(), "Menú " + menu.getIdMenu());
        }
    }

    private void cargarDatos() {
        modulos = moduloDAO.buscarConFiltros(filtroBusqueda, paginaActual, tamanoPagina);
        totalResultados = moduloDAO.contarConFiltros(filtroBusqueda);
        
        padres = moduloDAO.buscarPadresConFiltros(filtroBusquedaPadres, paginaActualPadres, tamanoPaginaPadres);
        totalResultadosPadres = moduloDAO.contarPadresConFiltros(filtroBusquedaPadres); 
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
        list.add(new BasePage.BreadcrumbItem("Seguridad", ModuloPage.class)); 
        list.add(new BasePage.BreadcrumbItem("Módulo", ModuloPage.class));
        return list;
    }
}