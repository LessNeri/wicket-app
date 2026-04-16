package com.miproject.pages.principal;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.dao.ClienteDAO;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.models.Cliente;
import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil;
import com.miproject.services.JWTService;

import org.apache.wicket.AttributeModifier;
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

import javax.servlet.http.Cookie;
import java.util.List;

public class ClientePage extends BasePage {

    private ClienteDAO clienteDAO = new ClienteDAO();
    private PerfilDAO perfilDAO = new PerfilDAO();
    private PermisoDAO permisoDAO = new PermisoDAO();
    
    private List<Cliente> clientes;
    private int paginaActual = 1;
    private int tamanoPagina = 5;
    private String filtroBusqueda = "";
    private int totalResultados;
    
    // Variables dinámicas
    private int idMenuOrigen; 
    private int idModulo; 
    private String nombreMenuActual; 

    private boolean puedeAgregar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeConsultar;

    private String menuPadre;
    private String menuHijo;

    public ClientePage(PageParameters parameters) {
        super();

        idMenuOrigen = parameters.get("origen").toInt(5); 
        idModulo = idMenuOrigen; 

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

        cargarDatos();

        add(new FeedbackPanel("feedback"));
        
        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        Perfil perfilActual = perfilDAO.obtenerPorId(idPerfilUsuario);
        boolean esAdmin = (perfilActual != null && perfilActual.isBitAdministrador());

        if (esAdmin) {
            puedeAgregar = true; puedeEditar = true; puedeEliminar = true; puedeConsultar = true;
        } else {
            PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, idModulo);
            if (permisos != null) {
                puedeAgregar = permisos.isBitAgregar();
                puedeEditar = permisos.isBitEditar();
                puedeEliminar = permisos.isBitEliminar();
                puedeConsultar = permisos.isBitConsulta();
            } else {
                puedeAgregar = false; puedeEditar = false; puedeEliminar = false; puedeConsultar = false;
            }
        }

        
        add(new Label("titulo", "Gestión de Clientes (" + nombreMenuActual + ")"));

        Label mensajeSinPermiso = new Label("mensajeSinPermiso", "No tienes permiso para ver esta página.");
        mensajeSinPermiso.setVisible(!puedeConsultar);
        add(mensajeSinPermiso);

        PageParameters paramsNuevo = new PageParameters();
        paramsNuevo.add("origen", idMenuOrigen);
        BookmarkablePageLink<Void> nuevoLink = new BookmarkablePageLink<>("nuevoCliente", CrearClientePage.class, paramsNuevo);
        nuevoLink.setVisible(puedeAgregar && puedeConsultar); 
        add(nuevoLink);

        WebMarkupContainer tablaContainer = new WebMarkupContainer("tablaContainer");
        tablaContainer.setOutputMarkupId(true);
        tablaContainer.setVisible(puedeConsultar);
        add(tablaContainer);

        WebMarkupContainer contenedorTabla = new WebMarkupContainer("contenedorTabla");
        contenedorTabla.setOutputMarkupId(true);
        tablaContainer.add(contenedorTabla);

        Label totalResultadosLabel = new Label("totalResultados", new PropertyModel<>(this, "totalResultados"));
        totalResultadosLabel.setVisible(puedeConsultar);
        totalResultadosLabel.setOutputMarkupId(true);

        Form<Void> busquedaForm = new Form<>("busquedaForm");
        busquedaForm.setVisible(puedeConsultar);
        tablaContainer.add(busquedaForm);

        TextField<String> busquedaField = new TextField<>("busqueda", new Model<>(""));
        busquedaField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filtroBusqueda = busquedaField.getModelObject();
                if (filtroBusqueda == null) filtroBusqueda = "";
                paginaActual = 1;
                cargarDatos();
                target.add(contenedorTabla);
                target.add(totalResultadosLabel);
            }
        });

        busquedaForm.add(busquedaField);
        busquedaForm.add(totalResultadosLabel);

        // Crear la tabla dentro del contenedor
        WebMarkupContainer tableWrapper = new WebMarkupContainer("tableWrapper");
        tableWrapper.setOutputMarkupId(true);
        contenedorTabla.add(tableWrapper);

        ListView<Cliente> clienteList = new ListView<Cliente>("clienteList", new PropertyModel<>(this, "clientes")) {
            @Override
            protected void populateItem(ListItem<Cliente> item) {
                Cliente cliente = item.getModelObject();
                item.add(new Label("codigo", cliente.getStrCodigoCliente()));
                item.add(new Label("nombre", cliente.getStrNombre()));
                item.add(new Label("telefono", cliente.getStrTelefono()));
                item.add(new Label("empresa", cliente.getStrEmpresa()));

                String textoEstado = cliente.getIdEstado() == 1 ? "ACTIVO" : "INACTIVO";
                String claseEstado = cliente.getIdEstado() == 1 ? "badge-activo" : "badge-inactivo";
                
                WebMarkupContainer spanEstado = new WebMarkupContainer("estado");
                spanEstado.add(new AttributeModifier("class", "badge " + claseEstado));
                spanEstado.add(new Label("estadoTexto", textoEstado));
                item.add(spanEstado);

                PageParameters paramsAccion = new PageParameters();
                paramsAccion.add("id", cliente.getId());
                paramsAccion.add("origen", idMenuOrigen);

                BookmarkablePageLink<Void> editarLink = new BookmarkablePageLink<>("editarCliente", EditarClientePage.class, paramsAccion);
                editarLink.setVisible(puedeEditar);
                item.add(editarLink);
                
                BookmarkablePageLink<Void> eliminarLink = new BookmarkablePageLink<>("eliminarCliente", EliminarClientePage.class, paramsAccion);
                eliminarLink.setVisible(puedeEliminar);
                item.add(eliminarLink);
            }
        };

        tableWrapper.add(clienteList);

        // Agregar la paginación también dentro del contenedor
        org.apache.wicket.model.IModel<String> infoModel = new org.apache.wicket.model.IModel<String>() {
            @Override
            public String getObject() {
                if (totalResultados == 0) return "No se encontraron registros";
                int desde = (paginaActual - 1) * tamanoPagina + 1;
                int hasta = Math.min(paginaActual * tamanoPagina, totalResultados);
                return "Mostrando " + desde + " a " + hasta + " de " + totalResultados + " registros";
            }
        };
        contenedorTabla.add(new Label("infoPagina", infoModel));

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnAnterior = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnAnterior") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (paginaActual > 1) {
                    paginaActual--;
                    cargarDatos();
                    target.add(contenedorTabla);
                }
            }
            
            @Override
            public boolean isEnabled() {
                return paginaActual > 1;
            }
        };
        contenedorTabla.add(btnAnterior);

        org.apache.wicket.ajax.markup.html.AjaxLink<Void> btnSiguiente = new org.apache.wicket.ajax.markup.html.AjaxLink<Void>("btnSiguiente") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                int totalPaginas = (int) Math.ceil((double) totalResultados / tamanoPagina);
                if (paginaActual < totalPaginas) {
                    paginaActual++;
                    cargarDatos();
                    target.add(contenedorTabla);
                }
            }

            @Override
            public boolean isEnabled() {
                int totalPaginas = (int) Math.ceil((double) totalResultados / tamanoPagina);
                return paginaActual < totalPaginas;
            }
        };
        contenedorTabla.add(btnSiguiente);
    }

    private void cargarDatos() {
        clientes = clienteDAO.buscarConFiltros(filtroBusqueda, idMenuOrigen, paginaActual, tamanoPagina);
        totalResultados = clienteDAO.contarConFiltros(filtroBusqueda, idMenuOrigen);
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
        list.add(new BasePage.BreadcrumbItem(menuPadre, HomePage.class)); 
        list.add(new BasePage.BreadcrumbItem(menuHijo, ClientePage.class));
        return list;
    } 
}