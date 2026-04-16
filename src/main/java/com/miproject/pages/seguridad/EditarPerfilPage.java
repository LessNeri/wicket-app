package com.miproject.pages.seguridad;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil; // <-- AGREGADO
import com.miproject.services.JWTService; // <-- AGREGADO
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO; // <-- AGREGADO

import org.apache.wicket.RestartResponseException; // <-- AGREGADO
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

public class EditarPerfilPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private Perfil perfilActual;
    private String nombrePerfil;
    private boolean esAdministrador;
    private String mensajeError;

    private PermisoDAO permisoDAO = new PermisoDAO(); 
    private static final int ID_MODULO_PERFIL = 1;

    public EditarPerfilPage(PageParameters parameters) {
        super();

        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        Perfil perfilSesion = perfilDAO.obtenerPorId(idPerfilUsuario); 
        boolean esAdmin = (perfilSesion != null && perfilSesion.isBitAdministrador());

        if (!esAdmin) {
            PermisoPerfil permisos = permisoDAO.obtenerPorPerfilYModulo(idPerfilUsuario, ID_MODULO_PERFIL);
            
            boolean puedeModificar = (permisos != null && permisos.isBitEditar());
            
            if (!puedeModificar) {
                getSession().error("Acceso denegado: No tienes permiso para editar perfiles.");
                throw new RestartResponseException(PerfilPage.class);
            }
        }

        int idPerfil = parameters.get("id").toInt();
        perfilActual = perfilDAO.obtenerPorId(idPerfil);

        if (perfilActual == null) {
            error("Perfil no encontrado");
            setResponsePage(PerfilPage.class);
            return;
        }

        // Cargar datos actuales
        nombrePerfil = perfilActual.getStrNombrePerfil();
        esAdministrador = perfilActual.isBitAdministrador();

        add(new Label("titulo", "Editar Perfil: " + nombrePerfil));

        Form<Void> form = new Form<>("formulario");

        // Campo nombre con validaciones
        TextField<String> nombreField = new TextField<>("nombre", new PropertyModel<>(this, "nombrePerfil"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(3, 30));
        
        // Validación de caracteres especiales
        nombreField.add(new IValidator<String>() {
            private final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s-]+$");
            
            @Override
            public void validate(IValidatable<String> validatable) {
                String valor = validatable.getValue();
                if (valor != null && !VALID_PATTERN.matcher(valor).matches()) {
                    ValidationError error = new ValidationError();
                    error.setMessage("El nombre solo puede contener letras, números, espacios y guiones. No se permiten símbolos especiales.");
                    validatable.error(error);
                }
            }
        });
        
        nombreField.setLabel(Model.of("Nombre del perfil"));
        form.add(nombreField);

        // Checkbox para administrador
        CheckBox adminCheck = new CheckBox("administrador", new PropertyModel<>(this, "esAdministrador"));
        form.add(adminCheck);

        // Mensaje de error
        Label errorLabel = new Label("mensajeError", new PropertyModel<>(this, "mensajeError"));
        errorLabel.setOutputMarkupId(true);
        errorLabel.setVisible(false);
        form.add(errorLabel);

        // Botón actualizar
        AjaxButton btnActualizar = new AjaxButton("btnActualizar") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // Validaciones
                if (nombrePerfil == null || nombrePerfil.trim().isEmpty()) {
                    mensajeError = "El nombre del perfil es obligatorio.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                if (nombrePerfil.length() > 30) {
                    mensajeError = "El nombre no puede exceder los 30 caracteres.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                // Verificar duplicados (excluyendo el perfil actual)
                if (!nombrePerfil.equals(perfilActual.getStrNombrePerfil()) && 
                    perfilDAO.existeNombre(nombrePerfil)) {
                    mensajeError = "Ya existe otro perfil con ese nombre.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                    return;
                }

                perfilActual.setStrNombrePerfil(nombrePerfil.trim());
                perfilActual.setBitAdministrador(esAdministrador);

                boolean exito = perfilDAO.actualizar(perfilActual);
                if (exito) {
                    getSession().info("¡El perfil '" + nombrePerfil.trim() + "' se actualizó correctamente!");
                    setResponsePage(PerfilPage.class);
                } else {
                    mensajeError = "Error al actualizar el perfil.";
                    errorLabel.setVisible(true);
                    target.add(errorLabel);
                }
            }
        };
        form.add(btnActualizar);

        BookmarkablePageLink<Void> btnCancelar = new BookmarkablePageLink<>("cancelar", PerfilPage.class);
        form.add(btnCancelar);

        add(form);
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
        list.add(new BasePage.BreadcrumbItem("Editar Perfil", EditarPerfilPage.class));
        return list;
    }
}