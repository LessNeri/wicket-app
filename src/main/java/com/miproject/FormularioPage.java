package com.miproject;

import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.EmailAddressValidator;

public class FormularioPage extends WebPage {

    private List<FileUpload> uploads = new java.util.ArrayList<>();
    private TextField<String> nombreField;
    private TextField<String> telefonoField;
    private EmailTextField emailField;
    
    public FormularioPage() {

        add(new Label("titulo", "Formulario de Validaciones"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        WebMarkupContainer ventanaConfirmacion = new WebMarkupContainer("ventanaConfirmacion");
        ventanaConfirmacion.setOutputMarkupId(true);
        ventanaConfirmacion.setVisible(false);
        add(ventanaConfirmacion);

        ventanaConfirmacion.add(new Link<Void>("btnRegresarInicio") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });

        Form<Void> formulario = new Form<>("formulario") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                
                this.setVisible(false);
                ventanaConfirmacion.setVisible(true);
                
                if (uploads != null && !uploads.isEmpty()) {
                    for (FileUpload upload : uploads) {
                        System.out.println("Archivo subido: " + upload.getClientFileName() + 
                                         " - Tamaño: " + upload.getSize() + " bytes" +
                                         " - Tipo: " + upload.getContentType());
                        
                        try {

                        } catch (Exception e) {
                            System.err.println("Error al guardar archivo: " + e.getMessage());
                        }
                    }
                }
                
                if (nombreField != null && nombreField.getConvertedInput() != null) {
                    DatabaseManager.insertarUsuario(
                        nombreField.getConvertedInput(),
                        "Formulario"
                    );
                }
            }
        };
        
        formulario.setMultiPart(true);
        formulario.setMaxSize(Bytes.megabytes(50));
        formulario.setFileMaxSize(Bytes.megabytes(10));
        
        add(formulario);

        nombreField = new TextField<>("nombre", Model.of(""));
        nombreField.setLabel(Model.of("Nombre"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(2, 70));
        nombreField.add(new PatternValidator("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(?:\\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$"));
        nombreField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                target.add(feedback);
            }
        });
        formulario.add(nombreField);

        emailField = new EmailTextField("email", Model.of(""));
        emailField.setLabel(Model.of("Correo electrónico"));
        emailField.setRequired(true);
        emailField.add(StringValidator.maximumLength(100));
        emailField.add(EmailAddressValidator.getInstance());
        emailField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                target.add(feedback);
            }
        });
        formulario.add(emailField);

        telefonoField = new TextField<>("telefono", Model.of(""));
        telefonoField.setLabel(Model.of("Teléfono"));
        telefonoField.setRequired(true);
        telefonoField.add(new PatternValidator("^\\d{10}$"));
        telefonoField.add(StringValidator.exactLength(10));
        telefonoField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String input = telefonoField.getInput();
                if (input != null && input.length() > 10) {
                    telefonoField.setModelObject(input.substring(0, 10));
                    error("El teléfono no puede tener más de 10 dígitos");
                }
                target.add(feedback);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                target.add(feedback);
            }
        });
        formulario.add(telefonoField);

        DateTextField fecha = new DateTextField(
                "fecha",
                Model.of(new Date()),
                "yyyy-MM-dd"
        );
        fecha.setLabel(Model.of("Fecha"));
        fecha.setRequired(true);
        fecha.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                target.add(feedback);
            }
        });
        formulario.add(fecha);

        FileUploadField archivos = new FileUploadField("archivos", 
            new PropertyModel<List<FileUpload>>(this, "uploads"));
        archivos.setLabel(Model.of("Archivos (imágenes, PDF, videos, Word, Excel)"));
        archivos.setRequired(true); // Hacer obligatorio
        formulario.add(archivos);

        formulario.add(new Button("enviar"));
    }
    
    public List<FileUpload> getUploads() {
        return uploads;
    }
    
    public void setUploads(List<FileUpload> uploads) {
        this.uploads = uploads;
    }
}