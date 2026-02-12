package com.miproject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;

public class WicketApplication extends WebApplication {

    private File uploadDir;

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();

        mountPage("/gestion-usuarios", GestionUsuariosPage.class);
        mountPage("/registrar", RegistrarUsuarioPage.class);
        mountPage("/editar", EditarUsuarioPage.class);
        mountPage("/eliminar", EliminarUsuarioPage.class);

        mountPage("/crud", CrudPage.class);

        mountPage("/api/usuarios", UsuariosApiPage.class);

        mountPage("/error/404", Error404Page.class);

        // ================= CONFIGURACIÓN GENERAL =================
        getDebugSettings().setComponentUseCheck(false);
        getDebugSettings().setAjaxDebugModeEnabled(false);
        getCspSettings().blocking().disabled();

        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");

        // ================= PÁGINAS DE ERROR PERSONALIZADAS =================
        // Sesión expirada
        getApplicationSettings().setPageExpiredErrorPage(Error404Page.class);

        // Error interno (500)
        getApplicationSettings().setInternalErrorPage(Error404Page.class);

        // Acceso no permitido
        getApplicationSettings().setAccessDeniedPage(Error404Page.class);

        // ================= DIRECTORIO DE SUBIDAS =================
        String path = System.getenv("UPLOAD_DIR");
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("UPLOAD_DIR no está configurada");
        }

        uploadDir = new File(path);
        uploadDir.mkdirs();

        // ================= RECURSOS ESTÁTICOS =================
        mountResource(
            "uploads/carrusel/${file}",
            new ResourceReference("carruselStatic") {

                @Override
                public AbstractResource getResource() {
                    return new AbstractResource() {

                        @Override
                        protected ResourceResponse newResourceResponse(Attributes attributes) {

                            String fileName = attributes.getParameters()
                                    .get("file")
                                    .toOptionalString();

                            if (fileName == null || fileName.contains("..")) {
                                return new ResourceResponse();
                            }

                            File file = new File(uploadDir, fileName);
                            if (!file.exists()) {
                                return new ResourceResponse();
                            }

                            ResourceResponse response = new ResourceResponse();
                            response.setContentType("image/jpeg");
                            response.setContentLength(file.length());

                            response.setWriteCallback(new WriteCallback() {
                                @Override
                                public void writeData(Attributes attributes) throws IOException {
                                    attributes.getResponse()
                                            .write(Files.readAllBytes(file.toPath()));
                                }
                            });

                            return response;
                        }
                    };
                }
            }
        );
    }
}
