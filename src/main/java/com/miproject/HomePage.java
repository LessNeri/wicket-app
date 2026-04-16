package com.miproject;

import org.apache.wicket.markup.html.basic.Label;

public class HomePage extends BasePage {

    public HomePage() {
        super(); 
        add(new Label("mensajeBienvenida", "Bienvenido al Sistema"));
    }
}