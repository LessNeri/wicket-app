package com.miproject;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class Error404Page extends WebPage {

    public Error404Page() {

        add(new Label("codigo", "404"));
        add(new Label("mensaje", "Página no disponible"));

        add(new Link<Void>("volverHome") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
    }
}