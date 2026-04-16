package com.miproject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.miproject.filters.JWTAuthenticationFilter;
import com.miproject.pages.LoginPage;
import com.miproject.pages.principal.ClientePage;
import com.miproject.pages.principal.CrearClientePage;
import com.miproject.pages.principal.EditarClientePage;
import com.miproject.pages.principal.EliminarClientePage;
import com.miproject.pages.seguridad.CrearUsuarioPage;
import com.miproject.pages.seguridad.EditarUsuarioPage;
import com.miproject.pages.seguridad.ModuloPage;
import com.miproject.pages.seguridad.PerfilPage;
import com.miproject.pages.seguridad.PermisoPage;
import com.miproject.pages.seguridad.UsuarioPage;

public class WicketApplication extends WebApplication {

    private File uploadDir;

    @Override
    public Class<? extends Page> getHomePage() {
        return LoginPage.class;
    }

    @Override
    protected void init() {
        super.init();

        getRequestCycleListeners().add(new JWTAuthenticationFilter());

        mountPage("/login", LoginPage.class);
        mountPage("/home", HomePage.class);
        mountPage("/perfil", PerfilPage.class);
        mountPage("/modulo", ModuloPage.class);
        mountPage("/permisos", PermisoPage.class);
        mountPage("/usuario", UsuarioPage.class);
        mountPage("/crear-usuario", CrearUsuarioPage.class);
        mountPage("/editar-usuario", EditarUsuarioPage.class);

        mountPage("/clientes", ClientePage.class);
        mountPage("/crear-cliente", CrearClientePage.class);
        mountPage("/editar-cliente", EditarClientePage.class);
        mountPage("/eliminar-cliente", EliminarClientePage.class);
        mountPage("/error/404", Error404Page.class);

        getDebugSettings().setComponentUseCheck(false);
        getDebugSettings().setAjaxDebugModeEnabled(false);
        getCspSettings().blocking().disabled();

        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");

        getApplicationSettings().setPageExpiredErrorPage(Error404Page.class);

        getApplicationSettings().setInternalErrorPage(Error404Page.class);

        getApplicationSettings().setAccessDeniedPage(Error404Page.class);

        String path = System.getenv("UPLOAD_DIR");
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("UPLOAD_DIR no está configurada");
        }

        uploadDir = new File(path);
        uploadDir.mkdirs();

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
