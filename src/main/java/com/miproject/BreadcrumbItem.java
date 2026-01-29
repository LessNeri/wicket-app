package com.miproject;

import org.apache.wicket.markup.html.WebPage;

public class BreadcrumbItem {

    private final String label;
    private final Class<? extends WebPage> pageClass;

    public BreadcrumbItem(String label, Class<? extends WebPage> pageClass) {
        this.label = label;
        this.pageClass = pageClass;
    }

    public String getLabel() {
        return label;
    }

    public Class<? extends WebPage> getPageClass() {
        return pageClass;
    }
}