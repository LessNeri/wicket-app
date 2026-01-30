package com.miproject;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;


public class InsertarDatosPage extends BasePage {

    public InsertarDatosPage() {

        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Inserción de datos", InsertarDatosPage.class)
        ));

        add(new Label("titulo", "Página de Inserción de Datos"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Form<Void> formInsertar = new Form<>("formInsertar") {
            @Override
            protected void onSubmit() {
                DatabaseManager.insertarUsuario("Ana", "García");
                success("Datos insertados correctamente en SQL");
            }
        };
        add(formInsertar);

        formInsertar.add(new Button("btnInsertar"));

        add(new Link<Void>("btnIrFormulario") {
            @Override
            public void onClick() {
                setResponsePage(FormularioPage.class);
            }
        });
    }
}