package com.miproject;

import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class FormularioPage extends BasePage {

    private List<FileUpload> uploads = new java.util.ArrayList<>();

    private TextField<String> nombreField;
    private TextField<String> telefonoField;
    private EmailTextField emailField;

    public FormularioPage() {

        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Inserción de datos", InsertarDatosPage.class),
            new BreadcrumbItem("Formulario", FormularioPage.class)
        ));

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

        // ================= FORMULARIO =================
        Form<Void> formulario = new Form<>("formulario") {
            @Override
            protected void onSubmit() {
                this.setVisible(false);
                ventanaConfirmacion.setVisible(true);

                if (nombreField.getModelObject() != null) {
                    DatabaseManager.insertarUsuario(
                        nombreField.getModelObject(),
                        "Formulario"
                    );
                }
            }
        };

        formulario.setMultiPart(true);
        formulario.setMaxSize(Bytes.megabytes(50));
        formulario.setFileMaxSize(Bytes.megabytes(10));
        add(formulario);

        // ================= NOMBRE =================
        nombreField = new TextField<>("nombre", Model.of(""));
        nombreField.setLabel(Model.of("Nombre"));
        nombreField.setRequired(true);
        nombreField.add(StringValidator.lengthBetween(2, 70));
        nombreField.add(new PatternValidator(
            "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(?:\\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$"
        ));
        nombreField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(nombreField);

        // ================= EMAIL =================
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
        });
        formulario.add(emailField);

        // ================= TELÉFONO =================
        telefonoField = new TextField<>("telefono", Model.of(""));
        telefonoField.setLabel(Model.of("Teléfono"));
        telefonoField.setRequired(true);
        telefonoField.add(new PatternValidator("^\\d{10}$"));
        telefonoField.add(StringValidator.exactLength(10));
        telefonoField.add(new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(telefonoField);

        // ================= FECHA DE NACIMIENTO =================
        DateTextField fecha = new DateTextField(
            "fecha",
            Model.of(new Date()),
            "yyyy-MM-dd"
        );
        fecha.setLabel(Model.of("Fecha de Nacimiento"));
        fecha.setRequired(true);

        fecha.add((IValidator<Date>) validatable -> {
            Date nacimiento = validatable.getValue();
            Date hoy = new Date();

            if (nacimiento.after(hoy)) {
                validatable.error(new ValidationError()
                    .setMessage("La fecha de nacimiento no puede ser futura"));
                return;
            }

            long edad = (hoy.getTime() - nacimiento.getTime())
                    / (1000L * 60 * 60 * 24 * 365);

            if (edad < 10 || edad > 120) {
                validatable.error(new ValidationError()
                    .setMessage("La edad debe estar entre 10 y 120 años"));
            }
        });

        fecha.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });

        formulario.add(fecha);


        // ================= BOTÓN =================
        formulario.add(new Button("enviar"));
    }

    public List<FileUpload> getUploads() {
        return uploads;
    }

    public void setUploads(List<FileUpload> uploads) {
        this.uploads = uploads;
    }
}