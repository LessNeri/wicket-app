package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.models.Perfil;
import com.miproject.models.Usuario;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.UsuarioDAO;
import com.miproject.services.AuthService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import java.io.File;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.markup.ComponentTag;
import java.util.Date;
import java.util.Base64;

import java.util.List;

public class CrearUsuarioPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String fechaNacimiento;
    private String telefono;
    private String correo;

    // Credenciales
    private String password;
    private String confirmPassword;

    // Configuración + Foto
    private List<FileUpload> imagenUpload;
    private Perfil perfilSeleccionado;
    private int estado = 1;

    private int pasoActual = 1;
    private WebMarkupContainer pasoContainer;
    private Label mensajeError;

    private FeedbackPanel feedback;
    private Form<Void> formGlobal;
    private AjaxButton btnSiguiente;

    private AjaxButton btnAnterior;

    public CrearUsuarioPage() {
        super();

        add(new Label("titulo", "Registro de Usuario"));

        formGlobal = new Form<>("formGlobal");
        formGlobal.setMultiPart(true);
        formGlobal.setOutputMarkupId(true);
        add(formGlobal);

        feedback = new FeedbackPanel("feedback");
        feedback.setFilter(message -> message.isError() || message.isFatal());
        feedback.setOutputMarkupId(true);
        feedback.setOutputMarkupPlaceholderTag(true);
        formGlobal.add(feedback);

        pasoContainer = new WebMarkupContainer("pasoContainer");
        pasoContainer.setOutputMarkupId(true);
        formGlobal.add(pasoContainer);

        mensajeError = new Label("mensajeError", Model.of(""));
        mensajeError.setOutputMarkupPlaceholderTag(true);
        mensajeError.setVisible(false);
        formGlobal.add(mensajeError);

        Label labelBotonSiguiente = new Label("label", Model.of("Siguiente"));
        labelBotonSiguiente.setOutputMarkupId(true);

        btnSiguiente = new AjaxButton("btnSiguiente") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (!validarPasoActual()) {
                    target.add(mensajeError);
                    return;
                }

                if (pasoActual < 3) {
                    pasoActual++;
                    mostrarPaso();
                } else {
                    guardarUsuario(target);
                }
                target.add(formGlobal);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedback);
            }
        };

        btnSiguiente.add(labelBotonSiguiente);
        formGlobal.add(btnSiguiente);

        btnAnterior = new AjaxButton("btnAnterior") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (pasoActual > 1) {
                    pasoActual--;
                    mostrarPaso();
                    target.add(formGlobal);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
            }
        };
        btnAnterior.setDefaultFormProcessing(false);

        btnAnterior.add(new org.apache.wicket.AttributeModifier("style", new org.apache.wicket.model.IModel<String>() {
            @Override
            public String getObject() {
                return pasoActual == 1 ? "display: none !important;" : "";
            }
        }));

        formGlobal.add(btnAnterior);

        formGlobal.add(new org.apache.wicket.markup.html.link.Link<Void>("btnCancelar") {
            @Override
            public void onClick() {
                setResponsePage(UsuarioPage.class);
            }
        });

        mostrarPaso();
    }

    private void mostrarPaso() {
        pasoContainer.removeAll();

        switch (pasoActual) {
            case 1:
                mostrarPaso1();
                break;
            case 2:
                mostrarPaso2();
                break;
            case 3:
                mostrarPaso3();
                break;
        }

        Label labelBoton = (Label) btnSiguiente.get("label");
        labelBoton.setDefaultModelObject(pasoActual == 3 ? "Guardar" : "Siguiente");

    }

    private void mostrarPaso1() {
        Fragment fragment = new Fragment("pasoContenido", "fragPaso1", this);

        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";

        String jsSoloLetras = "this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]/g, '');";

        fragment.add(new TextField<String>("nombre", new PropertyModel<>(this, "nombre")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "50");
                tag.put("pattern", regexLetras);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true).setLabel(Model.of("Nombre"))
                .add(StringValidator.maximumLength(50))
                .add(new org.apache.wicket.validation.validator.PatternValidator(regexLetras)));

        fragment.add(new TextField<String>("apellidoPaterno", new PropertyModel<>(this, "apellidoPaterno")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "50");
                tag.put("pattern", regexLetras);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true).setLabel(Model.of("Apellido Paterno"))
                .add(StringValidator.maximumLength(50))
                .add(new org.apache.wicket.validation.validator.PatternValidator(regexLetras)));

        fragment.add(new TextField<String>("apellidoMaterno", new PropertyModel<>(this, "apellidoMaterno")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("maxlength", "50");
                tag.put("pattern", regexLetras);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true).setLabel(Model.of("Apellido Materno"))
                .add(StringValidator.maximumLength(50))
                .add(new org.apache.wicket.validation.validator.PatternValidator(regexLetras)));

        fragment.add(new TextField<String>("fechaNacimiento", new PropertyModel<>(this, "fechaNacimiento")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("type", "date");

                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.time.LocalDate hace80 = hoy.minusYears(80);

                tag.put("max", hoy.toString());
                tag.put("min", hace80.toString());
            }
        }.setRequired(true).setLabel(Model.of("Fecha de Nacimiento")));

        fragment.add(new TextField<String>("telefono", new PropertyModel<>(this, "telefono")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("type", "tel");
                tag.put("maxlength", "10");
                tag.put("pattern", "[0-9]{10}");
                tag.put("oninput", "this.value = this.value.replace(/[^0-9]/g, '');");
            }
        }.setRequired(true).setLabel(Model.of("Teléfono"))
                .add(new org.apache.wicket.validation.validator.PatternValidator("^[0-9]{10}$")));

        pasoContainer.add(fragment);
    }

    private void mostrarPaso2() {
        Fragment fragment = new Fragment("pasoContenido", "fragPaso2", this);

        org.apache.wicket.markup.html.form.EmailTextField mailField = new org.apache.wicket.markup.html.form.EmailTextField(
                "correo", new PropertyModel<>(this, "correo"));

        mailField.setRequired(true)
                .setLabel(Model.of("Correo electrónico"))
                .add(StringValidator.maximumLength(100));

        fragment.add(mailField);

        PasswordTextField p1 = new PasswordTextField("password", new PropertyModel<>(this, "password"));
        p1.setRequired(true).add(StringValidator.minimumLength(8));
        p1.setResetPassword(false);

        PasswordTextField p2 = new PasswordTextField("confirmPassword", new PropertyModel<>(this, "confirmPassword"));
        p2.setRequired(true);
        p2.setResetPassword(false);

        fragment.add(p1);
        fragment.add(p2);

        java.util.List<org.apache.wicket.markup.html.form.validation.IFormValidator> validadoresPrevios = new java.util.ArrayList<>(
                formGlobal.getFormValidators());

        for (org.apache.wicket.markup.html.form.validation.IFormValidator v : validadoresPrevios) {
            if (v instanceof EqualPasswordInputValidator) {
                formGlobal.remove(v);
            }
        }

        formGlobal.add(new EqualPasswordInputValidator(p1, p2));

        pasoContainer.add(fragment);
    }

    private void mostrarPaso3() {
        Fragment fragment = new Fragment("pasoContenido", "fragPaso3", this);

        fragment.add(new DropDownChoice<Perfil>("perfil",
                new PropertyModel<>(this, "perfilSeleccionado"),
                perfilDAO.listarTodos(),
                new ChoiceRenderer<>("strNombrePerfil", "id")).setRequired(true));

        fragment.add(new DropDownChoice<Integer>("estado",
                new PropertyModel<>(this, "estado"),
                java.util.Arrays.asList(1, 0),
                new org.apache.wicket.markup.html.form.IChoiceRenderer<Integer>() {
                    @Override
                    public Object getDisplayValue(Integer object) {
                        return (object == 1) ? "Activo" : "Inactivo";
                    }

                    @Override
                    public String getIdValue(Integer object, int index) {
                        return object.toString();
                    }

                    @Override
                    public Integer getObject(String id,
                            org.apache.wicket.model.IModel<? extends java.util.List<? extends Integer>> choices) {
                        return Integer.valueOf(id);
                    }
                }).setRequired(true));

        fragment.add(new FileUploadField("imagen", new PropertyModel<>(this, "imagenUpload")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("accept", "image/jpeg, image/png, image/gif");
            }
        });

        pasoContainer.add(fragment);
    }

    private boolean validarPasoActual() {
        mensajeError.setVisible(false);

        switch (pasoActual) {
            case 1:
                if (nombre == null || nombre.trim().isEmpty()) {
                    mensajeError.setDefaultModelObject("El nombre es obligatorio");
                    mensajeError.setVisible(true);
                    return false;
                }
                if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty()) {
                    mensajeError.setDefaultModelObject("El apellido paterno es obligatorio");
                    mensajeError.setVisible(true);
                    return false;
                }
                if (apellidoMaterno == null || apellidoMaterno.trim().isEmpty()) {
                    mensajeError.setDefaultModelObject("El apellido materno es obligatorio");
                    mensajeError.setVisible(true);
                    return false;
                }
                if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
                    mensajeError.setDefaultModelObject("La fecha de nacimiento es obligatoria");
                    mensajeError.setVisible(true);
                    return false;
                }
                break;
            case 2:
                if (password == null || password.length() < 6) {
                    mensajeError.setDefaultModelObject("La contraseña debe tener al menos 6 caracteres");
                    mensajeError.setVisible(true);
                    return false;
                }
                if (!password.equals(confirmPassword)) {
                    mensajeError.setDefaultModelObject("Las contraseñas no coinciden");
                    mensajeError.setVisible(true);
                    return false;
                }
                break;
            case 3:
                if (perfilSeleccionado == null) {
                    mensajeError.setDefaultModelObject("Debe seleccionar un perfil");
                    mensajeError.setVisible(true);
                    return false;
                }
                break;
        }
        return true;
    }

    private void guardarUsuario(AjaxRequestTarget target) {
        try {
            Usuario usuarioExistente = usuarioDAO.obtenerPorCorreo(correo);
            if (usuarioExistente != null) {
                mensajeError.setDefaultModelObject("Este correo ya está registrado a nombre de otro usuario.");
                mensajeError.setVisible(true);
                target.add(mensajeError);
                return;
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setStrNombreUsuario(nombre);
            nuevoUsuario.setStrApellidoPaterno(apellidoPaterno);
            nuevoUsuario.setStrApellidoMaterno(apellidoMaterno);

            try {
                nuevoUsuario.setFechaNacimiento(java.sql.Date.valueOf(fechaNacimiento));
            } catch (IllegalArgumentException e) {
                mensajeError.setDefaultModelObject("Error en el formato de la fecha de nacimiento.");
                mensajeError.setVisible(true);
                target.add(mensajeError);
                return;
            }

            nuevoUsuario.setStrNumeroCelular(telefono);
            nuevoUsuario.setStrCorreo(correo);
            nuevoUsuario.setStrPwd(AuthService.encriptarPassword(password));
            nuevoUsuario.setIdPerfil(perfilSeleccionado.getId());
            nuevoUsuario.setIdEstadoUsuario(estado);

            if (imagenUpload != null && !imagenUpload.isEmpty()) {
                FileUpload archivoSeleccionado = imagenUpload.get(0);

                // ===== INICIO DE VALIDACIÓN DE IMAGEN =====
                String contentType = archivoSeleccionado.getContentType();
                if (contentType == null ||
                        (!contentType.equals("image/jpeg") &&
                                !contentType.equals("image/png") &&
                                !contentType.equals("image/gif"))) {
                    mensajeError.setDefaultModelObject("ERROR: El archivo debe ser una imagen (JPEG, PNG o GIF).");
                    mensajeError.setVisible(true);
                    target.add(mensajeError);
                    return;
                }

                String fileName = archivoSeleccionado.getClientFileName();
                if (fileName != null && !fileName.isEmpty()) {
                    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    if (!extension.equals("jpg") && !extension.equals("jpeg") &&
                            !extension.equals("png") && !extension.equals("gif")) {
                        mensajeError.setDefaultModelObject(
                                "ERROR: La extensión del archivo debe ser .jpg, .jpeg, .png o .gif");
                        mensajeError.setVisible(true);
                        target.add(mensajeError);
                        return;
                    }
                }
                // ===== FIN DE VALIDACIÓN DE IMAGEN =====

                try {
                    byte[] bytesImagen = archivoSeleccionado.getBytes();

                    if (bytesImagen != null && bytesImagen.length > 0) {
                        String base64Texto = Base64.getEncoder().encodeToString(bytesImagen);
                        String base64Completo = "data:" + contentType + ";base64," + base64Texto;
                        nuevoUsuario.setStrImagenUrl(base64Completo);
                    } else {
                        // Si el archivo está vacío, usar avatar por defecto
                        String inicial = nombre.substring(0, 1).toUpperCase();
                        String colorHex = generarColorAleatorio();
                        String urlAvatarGenerado = "https://ui-avatars.com/api/?name=" + inicial + "&background="
                                + colorHex + "&color=fff";
                        nuevoUsuario.setStrImagenUrl(urlAvatarGenerado);
                    }

                } catch (Exception e) {
                    String inicial = nombre.substring(0, 1).toUpperCase();
                    String colorHex = generarColorAleatorio();
                    String urlAvatarGenerado = "https://ui-avatars.com/api/?name=" + inicial + "&background=" + colorHex
                            + "&color=fff";
                    nuevoUsuario.setStrImagenUrl(urlAvatarGenerado);
                }
            } else {
                String inicial = nombre.substring(0, 1).toUpperCase();
                String colorHex = generarColorAleatorio();

                String urlAvatarGenerado = "https://ui-avatars.com/api/?name=" + inicial + "&background=" + colorHex
                        + "&color=fff";
                nuevoUsuario.setStrImagenUrl(urlAvatarGenerado);
            }
            boolean exito = usuarioDAO.crear(nuevoUsuario);
            if (exito) {
                getSession().success("¡El usuario ha sido registrado correctamente!");
                setResponsePage(UsuarioPage.class);
            } else {
                mensajeError.setDefaultModelObject("Error al guardar el usuario en base de datos");
                mensajeError.setVisible(true);
                target.add(mensajeError);
            }

        } catch (Exception e) {
            mensajeError.setDefaultModelObject("Error general: " + e.getMessage());
            mensajeError.setVisible(true);
            target.add(mensajeError);
        }
    }

    private String generarColorAleatorio() {
        String[] colores = { "1abc9c", "2ecc71", "3498db", "9b59b6", "f1c40f", "e67e22", "e74c3c", "34495e" };
        int index = (int) (Math.random() * colores.length);
        return colores[index];
    }

    @Override
    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BreadcrumbItem("Seguridad", HomePage.class));
        list.add(new BreadcrumbItem("Usuario", UsuarioPage.class));
        list.add(new BreadcrumbItem("Nuevo Usuario", CrearUsuarioPage.class));
        return list;
    }
}