package com.miproject;

import java.util.List;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

public abstract class BasePage extends WebPage {

    public BasePage(List<BreadcrumbItem> breadcrumbs) {

        add(new ListView<BreadcrumbItem>("breadcrumbs", breadcrumbs) {
            @Override
            protected void populateItem(ListItem<BreadcrumbItem> item) {
                BreadcrumbItem bc = item.getModelObject();

                // USAR AjaxLink EN LUGAR DE Link
                AjaxLink<Void> link = new AjaxLink<Void>("link") {
                    @Override
                    public void onClick(org.apache.wicket.ajax.AjaxRequestTarget target) {
                        setResponsePage(bc.getPageClass());
                    }
                };
                
                link.add(new Label("label", bc.getLabel()));
                item.add(link);
            }
        });
    }
}