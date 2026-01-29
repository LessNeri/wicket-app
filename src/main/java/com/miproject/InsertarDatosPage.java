package com.miproject;

import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class InsertarDatosPage extends BasePage {

    public InsertarDatosPage() {
        // Breadcrumbs para esta página
        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class),
            new BreadcrumbItem("Inserción de datos", InsertarDatosPage.class)
        ));
        
        // El resto del código SE MANTIENE EXACTAMENTE IGUAL
        add(new Label("titulo", "Página de Inserción de Datos"));
        
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        
        Form<Void> formInsertar = new Form<>("formInsertar") {
            @Override
            protected void onSubmit() {
                DatabaseManager.insertarUsuario("Ana", "García");
                success("Datos insertados correctamente");
            }
        };
        add(formInsertar);
        
        Button btnInsertar = new Button("btnInsertar");
        formInsertar.add(btnInsertar);
        
        add(new Link<Void>("btnIrFormulario") {
            @Override
            public void onClick() {
                setResponsePage(FormularioPage.class);
            }
        });
        
    }
}