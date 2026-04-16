package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.components.PaginacionPanel;
import com.miproject.models.Usuario;
import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil;
import com.miproject.dao.UsuarioDAO;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.services.JWTService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import javax.servlet.http.Cookie;
import java.util.List;

public class UsuarioPage extends BasePage {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private PermisoDAO permisoDAO = new PermisoDAO();
    private List<Usuario> usuarios;
    private int paginaActual = 1;
    private int tamanoPagina = 5;
    private String filtroBusqueda = "";
    private int totalResultados;

    private static final int ID_MODULO_USUARIO = 4;
    private boolean puedeAgregar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeConsultar;

    public UsuarioPage() {
        super();

        cargarDatos();

        // Obtener permisos del perfil logueado
        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, ID_MODULO_USUARIO);

        if (permisos != null) {
            puedeAgregar = permisos.isBitAgregar();
            puedeEditar = permisos.isBitEditar();
            puedeEliminar = permisos.isBitEliminar();
            puedeConsultar = permisos.isBitConsulta();
        } else {
            puedeAgregar = false;
            puedeEditar = false;
            puedeEliminar = false;
            puedeConsultar = false;
        }

        Label msjPermiso = new Label("mensajeSinPermiso", ""); // El texto ya está en el HTML
        msjPermiso.setVisible(!puedeConsultar);
        add(msjPermiso);

        Label titulo = new Label("titulo", "Gestión de Usuarios");
        titulo.setVisible(puedeConsultar);
        add(titulo);

        Label totalRes = new Label("totalResultados", String.valueOf(totalResultados));
        totalRes.setVisible(puedeConsultar);
        add(totalRes);

        // Botón nuevo usuario
        BookmarkablePageLink<Void> nuevoLink = new BookmarkablePageLink<>("nuevoUsuario", CrearUsuarioPage.class);
        nuevoLink.setVisible(puedeAgregar && puedeConsultar);
        add(nuevoLink);

        // 1. PRIMERO CREAMOS LOS CONTENEDORES (Para que el buscador los reconozca)
        WebMarkupContainer tablaContainer = new WebMarkupContainer("tablaContainer");
        tablaContainer.setOutputMarkupId(true);
        tablaContainer.setVisible(puedeConsultar);
        add(tablaContainer);

        PaginacionPanel paginacionPanel = new PaginacionPanel("paginacionContainer", paginaActual, totalResultados,
                tamanoPagina) {
            @Override
            public void onPageChange(int nuevaPagina, AjaxRequestTarget target) {
                paginaActual = nuevaPagina;
                cargarDatos();
                target.add(tablaContainer, this);
            }
        };

        paginacionPanel.setOutputMarkupId(true);
        add(paginacionPanel);

        // 2. LUEGO CREAMOS EL BUSCADOR
        Form<Void> busquedaForm = new Form<>("busquedaForm");
        busquedaForm.setVisible(puedeConsultar);
        TextField<String> busquedaField = new TextField<>("busqueda", new Model<>(""));
        busquedaField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Protegemos contra nulos
                filtroBusqueda = busquedaField.getModelObject() != null ? busquedaField.getModelObject() : "";
                paginaActual = 1;
                cargarDatos();
                target.add(tablaContainer);
            }
        });
        busquedaForm.add(busquedaField);
        add(busquedaForm);

        ListView<Usuario> listaUsuarios = new ListView<Usuario>("usuarioList",
                new org.apache.wicket.model.PropertyModel<>(this, "usuarios")) {
            @Override
            protected void populateItem(ListItem<Usuario> item) {
                Usuario usuario = item.getModelObject();

                String nombreStr = usuario.getStrNombreUsuario() != null ? usuario.getStrNombreUsuario().trim() : "";
                String apellidoPaternoStr = usuario.getStrApellidoPaterno() != null
                        ? usuario.getStrApellidoPaterno().trim()
                        : "";

                String primerNombre = nombreStr.contains(" ") ? nombreStr.split(" ")[0] : nombreStr;
                String primerApellido = apellidoPaternoStr.contains(" ") ? apellidoPaternoStr.split(" ")[0]
                        : apellidoPaternoStr;

                String imgUrl = usuario.getStrImagenUrl();

                if (imgUrl == null || imgUrl.trim().isEmpty()) {
                    String nombreParaAvatar = primerNombre + "+" + primerApellido;
                    imgUrl = "https://ui-avatars.com/api/?name=" + nombreParaAvatar
                            + "&background=random&color=fff&size=128";
                }

                org.apache.wicket.markup.html.WebMarkupContainer imgContainer = new org.apache.wicket.markup.html.WebMarkupContainer(
                        "imagen");
                imgContainer.add(new org.apache.wicket.AttributeModifier("src", imgUrl));
                item.add(imgContainer);

                item.add(new Label("nombreCompleto", (primerNombre + " " + primerApellido).trim()));
                item.add(new Label("correo", usuario.getStrCorreo()));
                item.add(new Label("perfil", obtenerNombrePerfil(usuario.getIdPerfil())));
                item.add(new Label("celular", usuario.getStrNumeroCelular()));

                String estadoTexto = usuario.getIdEstadoUsuario() == 1 ? "Activo" : "Inactivo";
                String estadoColor = usuario.getIdEstadoUsuario() == 1 ? "#2ecc71" : "#e74c3c";
                Label estadoLabel = new Label("estado", estadoTexto);
                estadoLabel.add(new org.apache.wicket.AttributeModifier("style",
                        "color: " + estadoColor + "; font-weight: 500;"));
                item.add(estadoLabel);

                PageParameters params = new PageParameters();
                params.add("id", usuario.getId());

                BookmarkablePageLink<Void> editarLink = new BookmarkablePageLink<>("editarUsuario",
                        EditarUsuarioPage.class, params);
                editarLink.setVisible(puedeEditar);
                item.add(editarLink);

                BookmarkablePageLink<Void> eliminarLink = new BookmarkablePageLink<>("eliminarUsuario",
                        EliminarUsuarioPage.class, params);
                eliminarLink.setVisible(puedeEliminar);
                item.add(eliminarLink);
            }
        };

        tablaContainer.add(listaUsuarios);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        feedback.setFilter(new org.apache.wicket.feedback.IFeedbackMessageFilter() {
            @Override
            public boolean accept(org.apache.wicket.feedback.FeedbackMessage message) {
                return message.isSuccess();
            }
        });
        add(feedback);

    }

    // Agrega este nuevo método justo debajo del constructor que acabamos de cerrar:
    public String getTextoPaginacion() {
        if (totalResultados == 0)
            return "Mostrando 0 registros";
        int desde = (paginaActual - 1) * tamanoPagina + 1;
        int hasta = Math.min(paginaActual * tamanoPagina, totalResultados);
        return "Mostrando " + desde + " a " + hasta + " de " + totalResultados + " registros";
    }

    private void cargarDatos() {
        usuarios = usuarioDAO.buscarConFiltros(filtroBusqueda, paginaActual, tamanoPagina);
        totalResultados = usuarioDAO.contarConFiltros(filtroBusqueda);
    }

    private String obtenerNombrePerfil(int idPerfil) {
        PerfilDAO perfilDAO = new PerfilDAO();
        Perfil perfil = perfilDAO.obtenerPorId(idPerfil);
        return perfil != null ? perfil.getStrNombrePerfil() : "Sin perfil";
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
    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BreadcrumbItem("Seguridad", HomePage.class));
        list.add(new BreadcrumbItem("Usuario", UsuarioPage.class));
        return list;
    }
}