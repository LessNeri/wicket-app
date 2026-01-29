package com.miproject;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class InsertarDatosPage extends WebPage {
    
    public InsertarDatosPage() {
        add(new Label("titulo", "Página de Inserción de Datos"));
        
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        
        add(new Link<Void>("btnIrFormulario") {
            @Override
            public void onClick() {
                setResponsePage(FormularioPage.class);
            }
        });
        
        add(new Link<Void>("btnRegresarHome") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
    }
}