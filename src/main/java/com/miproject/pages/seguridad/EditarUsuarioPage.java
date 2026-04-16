package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.HomePage;
import com.miproject.models.Perfil;
import com.miproject.models.Usuario;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.UsuarioDAO;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.util.lang.Bytes;
import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.Base64;

public class EditarUsuarioPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioActual;

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;

    private String fechaNacimientoStr; 
    private List<FileUpload> imagenUpload;

    private final String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private final String jsSoloLetras = "this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]/g, '');";

    private Perfil perfilSeleccionado;
    private int estado;
    private Label mensajeError;

    private FeedbackPanel feedback;

    public EditarUsuarioPage(PageParameters parameters) {
        super();

        int idUsuario = parameters.get("id").toInt();
        usuarioActual = usuarioDAO.obtenerPorId(idUsuario);

        if (usuarioActual == null) {
            getSession().error("Usuario no encontrado");
            setResponsePage(UsuarioPage.class);
            return;
        }

        // Cargar datos
        nombre = usuarioActual.getStrNombreUsuario();
        apellidoPaterno = usuarioActual.getStrApellidoPaterno();
        apellidoMaterno = usuarioActual.getStrApellidoMaterno();
        telefono = usuarioActual.getStrNumeroCelular();
        fechaNacimientoStr = (usuarioActual.getFechaNacimiento() != null) ? usuarioActual.getFechaNacimiento().toString() : "";
        perfilSeleccionado = perfilDAO.obtenerPorId(usuarioActual.getIdPerfil());
        estado = usuarioActual.getIdEstadoUsuario();

feedback = new FeedbackPanel("mensajeError");
feedback.setOutputMarkupId(true);
// Filtro opcional: Solo mostrar Errores y Advertencias aquí
feedback.setFilter(message -> message.isError() || message.isWarning());
add(feedback);

add(new Label("titulo", "Editar Usuario"));

        Form<Void> editForm = new Form<>("editForm");
        editForm.setMultiPart(true);
        editForm.setMaxSize(Bytes.megabytes(5));
        add(editForm);

        editForm.add(new TextField<String>("nombre", new PropertyModel<>(this, "nombre")) {
            @Override protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true)
         .add(StringValidator.maximumLength(50))
         .add(new PatternValidator(regexLetras)));

        editForm.add(new TextField<String>("apellidoPaterno", new PropertyModel<>(this, "apellidoPaterno")) {
            @Override protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true)
         .add(StringValidator.maximumLength(50))
         .add(new PatternValidator(regexLetras)));

        editForm.add(new TextField<String>("apellidoMaterno", new PropertyModel<>(this, "apellidoMaterno")) {
            @Override protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("oninput", jsSoloLetras);
            }
        }.setRequired(true)
         .add(StringValidator.maximumLength(50))
         .add(new PatternValidator(regexLetras)));

        editForm.add(new TextField<String>("telefono", new PropertyModel<>(this, "telefono")) {
            @Override protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("type", "tel");
                tag.put("maxlength", "10");
                tag.put("oninput", "this.value = this.value.replace(/[^0-9]/g, '');");
            }
        }.setRequired(true).add(new PatternValidator("^[0-9]{10}$")));

        // FECHA: Aquí ocurre la magia para evitar el RuntimeException
        editForm.add(new TextField<String>("fechaNacimiento", new PropertyModel<>(this, "fechaNacimientoStr")) {
            @Override protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("type", "date"); // Obligamos a que sea date en el navegador
                tag.put("max", java.time.LocalDate.now().toString());
            }
        }.setRequired(true));

        // SELECTORES (Usando los Renderers correctos)
        editForm.add(new DropDownChoice<Perfil>("perfil", 
            new PropertyModel<>(this, "perfilSeleccionado"), 
            perfilDAO.listarTodos(),
            new ChoiceRenderer<>("strNombrePerfil", "id")
        ).setRequired(true));

        editForm.add(new DropDownChoice<Integer>("estado", 
            new PropertyModel<>(this, "estado"), 
            Arrays.asList(1, 0),
            new IChoiceRenderer<Integer>() {
                @Override public Object getDisplayValue(Integer object) { return (object == 1) ? "Activo" : "Inactivo"; }
                @Override public String getIdValue(Integer object, int index) { return object.toString(); }
                @Override public Integer getObject(String id, org.apache.wicket.model.IModel<? extends List<? extends Integer>> choices) { return Integer.valueOf(id); }
            }
        ).setRequired(true));

        editForm.add(new FileUploadField("imagen", new PropertyModel<>(this, "imagenUpload")));

        editForm.add(new AjaxButton("btnSiguiente") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                actualizarUsuario(target);
            }
@Override
protected void onError(AjaxRequestTarget target) {
    target.add(feedback);
}
        });

        editForm.add(new org.apache.wicket.markup.html.link.Link<Void>("btnCancelar") {
            @Override public void onClick() { setResponsePage(UsuarioPage.class); }
        });
    }

    private void actualizarUsuario(AjaxRequestTarget target) {
        try {
            usuarioActual.setStrNombreUsuario(nombre);
            usuarioActual.setStrApellidoPaterno(apellidoPaterno);
            usuarioActual.setStrApellidoMaterno(apellidoMaterno);
            usuarioActual.setStrNumeroCelular(telefono);
            
            if (fechaNacimientoStr != null && !fechaNacimientoStr.isEmpty()) {
                usuarioActual.setFechaNacimiento(java.sql.Date.valueOf(fechaNacimientoStr));
            }

            if (perfilSeleccionado != null) {
                usuarioActual.setIdPerfil(perfilSeleccionado.getId());
            }
            usuarioActual.setIdEstadoUsuario(estado);

            // Imagen
if (imagenUpload != null && !imagenUpload.isEmpty()) {
    FileUpload archivo = imagenUpload.get(0);
    
    byte[] bytesImagen = archivo.getBytes();
    
    String base64Texto = Base64.getEncoder().encodeToString(bytesImagen);
    
    String mimeType = archivo.getContentType();
    
    String base64Completo = "data:" + mimeType + ";base64," + base64Texto;
    
    usuarioActual.setStrImagenUrl(base64Completo);
}

            if (usuarioDAO.actualizar(usuarioActual)) {
                getSession().success("¡Usuario actualizado!");
                setResponsePage(UsuarioPage.class);
            } else {
                throw new Exception("Error al guardar en base de datos.");
            }

} catch (Exception e) {
    error("Error: " + e.getMessage()); 
    target.add(feedback);
}
    }

    @Override
    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BreadcrumbItem("Seguridad", HomePage.class)); 
        list.add(new BreadcrumbItem("Usuario", UsuarioPage.class));
        list.add(new BreadcrumbItem("Editar Usuario", EditarUsuarioPage.class));
        return list;
    }
}