package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.models.Usuario;
import com.miproject.dao.UsuarioDAO;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import java.util.List;

public class EliminarUsuarioPage extends BasePage {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuario;

    public EliminarUsuarioPage(PageParameters parameters) {
        super();

        int idUsuario = parameters.get("id").toInt();
        usuario = usuarioDAO.obtenerPorId(idUsuario);

        if (usuario == null) {
            error("Usuario no encontrado");
            setResponsePage(UsuarioPage.class);
            return;
        }

        add(new Label("titulo", "Eliminar Usuario"));
        add(new Label("nombreUsuario", usuario.getStrNombreUsuario()));
        add(new Label("correo", usuario.getStrCorreo()));
        add(new Label("telefono", usuario.getStrNumeroCelular()));

        // Mostrar estado
        String estadoTexto = usuario.getIdEstadoUsuario() == 1 ? "Activo" : "Inactivo";
        String estadoColor = usuario.getIdEstadoUsuario() == 1 ? "#2ecc71" : "#e74c3c";
        Label estadoLabel = new Label("estado", estadoTexto);
        estadoLabel.add(new org.apache.wicket.AttributeModifier("style", "color: " + estadoColor + "; font-weight: 500;"));
        add(estadoLabel);

        // Mensaje de advertencia
        add(new Label("mensajeAdvertencia", "Esta acción no se puede deshacer. ¿Está seguro de eliminar este usuario?"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        // Botón confirmar
        add(new Link<Void>("confirmar") {
            @Override
            public void onClick() {
                String imagenUrl = usuario.getStrImagenUrl();
                boolean exito = usuarioDAO.eliminar(usuario.getId());

                if (exito) {
                    if (imagenUrl != null && !imagenUrl.isEmpty()) {
                        try {
                            String rutaDirectorio = org.apache.wicket.protocol.http.WebApplication.get().getServletContext().getRealPath("/imagenes_proyecto/");
                            java.io.File archivoImagen = new java.io.File(rutaDirectorio + java.io.File.separator + imagenUrl);
                            if (archivoImagen.exists()) { archivoImagen.delete(); }
                        } catch (Exception e) { /* Ignorar si falla el borrado */ }
                    }
                    
                    getSession().info("Usuario eliminado exitosamente.");
                    setResponsePage(UsuarioPage.class);

                } else {
                    error("Error al eliminar el usuario de la base de datos.");
                }
            }
        });

        // Botón cancelar
        add(new Link<Void>("cancelar") {
            @Override
            public void onClick() {
                setResponsePage(UsuarioPage.class);
            }
        });
    }

    @Override
    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BreadcrumbItem("Seguridad", HomePage.class)); 
        list.add(new BreadcrumbItem("Usuario", UsuarioPage.class));
        list.add(new BreadcrumbItem("Eliminar Usuario", EliminarUsuarioPage.class));
        
        return list;
    }
}