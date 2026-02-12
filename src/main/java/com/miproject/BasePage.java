package com.miproject;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class BasePage extends WebPage {

    public BasePage(List<BreadcrumbItem> breadcrumbs) {

        add(new ListView<BreadcrumbItem>("breadcrumbs", breadcrumbs) {
            @Override
            protected void populateItem(ListItem<BreadcrumbItem> item) {
                BreadcrumbItem bc = item.getModelObject();
                
                // SOLUCIÓN: Usar WebMarkupContainer en lugar de cualquier Link
                WebMarkupContainer linkContainer = new WebMarkupContainer("link");
                
                // Generar la URL
                String url = urlFor(bc.getPageClass(), new PageParameters()).toString();
                
                // Añadir atributo onclick con JavaScript
                linkContainer.add(new org.apache.wicket.AttributeModifier("onclick", 
                    "window.location.href='" + url + "'; return false;"));
                
                // Añadir estilo de cursor pointer
                linkContainer.add(new org.apache.wicket.AttributeModifier("style", 
                    "cursor: pointer;"));
                
                linkContainer.add(new Label("label", bc.getLabel()));
                item.add(linkContainer);
            }
        });
    }
}