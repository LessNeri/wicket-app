package com.miproject;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.extensions.markup.html.form.DateTextField;

public class FormularioPage extends WebPage {

    public FormularioPage() {

        add(new Label("titulo", "Formulario de Validaciones"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Form<Void> formulario = new Form<>("formulario");
        add(formulario);

        TextField<String> nombre = new TextField<>("nombre", Model.of(""));
        nombre.setLabel(Model.of("Nombre"));
        nombre.setRequired(true);
        nombre.add(StringValidator.lengthBetween(2, 30));
        nombre.add(new PatternValidator("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"));
        nombre.setOutputMarkupId(true);
        nombre.add(new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(nombre);

        TextField<String> edad = new TextField<>("edad", Model.of(""));
        edad.setLabel(Model.of("Edad"));
        edad.setRequired(true);
        edad.add(new PatternValidator("^\\d+$"));
        edad.setOutputMarkupId(true);
        edad.add(new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(edad);

        EmailTextField email = new EmailTextField("email", Model.of(""));
        email.setLabel(Model.of("Correo electrónico"));
        email.setRequired(true);
        email.setOutputMarkupId(true);
        email.add(new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(email);

        TextField<String> telefono = new TextField<>("telefono", Model.of(""));
        telefono.setLabel(Model.of("Teléfono"));
        telefono.setRequired(true);
        telefono.add(new PatternValidator("^\\d{10}$"));
        telefono.setOutputMarkupId(true);
        telefono.add(new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(telefono);

        DateTextField fecha = new DateTextField(
                "fecha",
                Model.of(new Date()),
                "yyyy-MM-dd"
        );
        fecha.setLabel(Model.of("Fecha"));
        fecha.setRequired(true);
        fecha.setOutputMarkupId(true);
        fecha.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        formulario.add(fecha);

        formulario.add(new Button("enviar") {
    @Override
    public void onSubmit() {
        // SOLO si el formulario es válido
        setResponsePage(Error404Page.class);
    }
});

    }
}