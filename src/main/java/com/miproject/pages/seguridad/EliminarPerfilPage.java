package com.miproject.pages.seguridad;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil; // <-- AGREGADO
import com.miproject.services.JWTService; // <-- AGREGADO
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO; // <-- AGREGADO

import org.apache.wicket.RestartResponseException; // <-- AGREGADO
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import java.util.List;

import javax.servlet.http.Cookie;

public class EliminarPerfilPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private Perfil perfil;

    private PermisoDAO permisoDAO = new PermisoDAO();
    private static final int ID_MODULO_PERFIL = 1;

    public EliminarPerfilPage(PageParameters parameters) {
        super();

        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        Perfil perfilSesion = perfilDAO.obtenerPorId(idPerfilUsuario);
        boolean esAdmin = (perfilSesion != null && perfilSesion.isBitAdministrador());

        if (!esAdmin) {
            PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, ID_MODULO_PERFIL);
            
            boolean puedeEliminar = (permisos != null && permisos.isBitEliminar());
            
            if (!puedeEliminar) {
                getSession().error("Acceso denegado: No tienes permiso para eliminar perfiles.");
                throw new RestartResponseException(PerfilPage.class);
            }
        }

        int idPerfil = parameters.get("id").toInt();
        perfil = perfilDAO.obtenerPorId(idPerfil);

        if (perfil == null) {
            error("Perfil no encontrado");
            setResponsePage(PerfilPage.class);
            return;
        }

        // Verificar si hay usuarios asociados a este perfil
        boolean tieneUsuarios = perfilDAO.tieneUsuariosAsociados(idPerfil);
        String mensajeAdvertencia = "";
        if (tieneUsuarios) {
            mensajeAdvertencia = "Este perfil tiene usuarios asociados. Si lo eliminas, esos usuarios quedarán sin perfil asignado.";
        }

        add(new Label("titulo", "Eliminar Perfil"));
        add(new Label("nombrePerfil", perfil.getStrNombrePerfil()));
        
        // Mostrar rol
        String rolTexto = perfil.isBitAdministrador() ? "Administrador" : "Usuario";
        add(new Label("rolPerfil", rolTexto));

        // Mensaje de advertencia
        Label warningLabel = new Label("mensajeAdvertencia", mensajeAdvertencia);
        warningLabel.setVisible(tieneUsuarios);
        add(warningLabel);

        // Botón confirmar
        add(new Link<Void>("confirmar") {
            @Override
            public void onClick() {
                boolean exito = perfilDAO.eliminar(perfil.getId());
                if (exito) {
                    getSession().info("¡El perfil se eliminó correctamente!");
                    setResponsePage(PerfilPage.class);
                } else {
                    error("Error al eliminar el perfil.");
                }
            }
        });

        // Botón cancelar
        add(new Link<Void>("cancelar") {
            @Override
            public void onClick() {
                setResponsePage(PerfilPage.class);
            }
        });

        add(new FeedbackPanel("feedback"));
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
        list.add(new BasePage.BreadcrumbItem("Perfil", PerfilPage.class)); // El nivel que faltaba
        list.add(new BasePage.BreadcrumbItem("Eliminar Perfil", EliminarPerfilPage.class));
        return list;
    }
}