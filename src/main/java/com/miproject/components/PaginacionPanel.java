package com.miproject.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.behavior.AttributeAppender;

import java.util.List;
import java.util.stream.IntStream;

public abstract class PaginacionPanel extends Panel {

    private int paginaActual;
    private int totalResultados;
    private int tamanoPagina;

    public PaginacionPanel(String id, int paginaActual, int totalResultados, int tamanoPagina) {
        super(id);
        this.paginaActual = paginaActual;
        this.totalResultados = totalResultados;
        this.tamanoPagina = tamanoPagina;

        // Esencial para que el panel se pueda actualizar por AJAX
        setOutputMarkupId(true);

        Label info = new Label("infoPagina", () -> getTextoPaginacion());
        info.setOutputMarkupId(true);
        add(info);

        // BOTÓN ANTERIOR
        AjaxLink<Void> btnAnterior = new AjaxLink<Void>("btnAnterior") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (PaginacionPanel.this.paginaActual > 1) {
                    PaginacionPanel.this.paginaActual--;
                    target.add(PaginacionPanel.this); // Actualiza la vista
                    onPageChange(PaginacionPanel.this.paginaActual, target);
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // Wicket deshabilita el botón automáticamente si es la página 1
                setEnabled(PaginacionPanel.this.paginaActual > 1);
            }
        };
        add(btnAnterior);

        // BOTÓN SIGUIENTE
        AjaxLink<Void> btnSiguiente = new AjaxLink<Void>("btnSiguiente") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                int totalPaginas = getTotalPaginas();
                if (PaginacionPanel.this.paginaActual < totalPaginas) {
                    PaginacionPanel.this.paginaActual++;
                    target.add(PaginacionPanel.this); // Actualiza la vista
                    onPageChange(PaginacionPanel.this.paginaActual, target);
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // Wicket deshabilita el botón si estamos en la última página
                setEnabled(PaginacionPanel.this.paginaActual < getTotalPaginas());
            }
        };
        add(btnSiguiente);

        // LISTA DE NÚMEROS DE PÁGINA
        add(new ListView<Integer>("paginas", () -> getPaginas()) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                final Integer num = item.getModelObject();

                AjaxLink<Void> link = new AjaxLink<Void>("linkPagina") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        if (!num.equals(PaginacionPanel.this.paginaActual)) {
                            PaginacionPanel.this.paginaActual = num;
                            target.add(PaginacionPanel.this); // Actualiza la vista
                            onPageChange(PaginacionPanel.this.paginaActual, target);
                        }
                    }

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        // Deshabilitar el clic si ya es la página actual
                        setEnabled(!num.equals(PaginacionPanel.this.paginaActual));
                    }
                };

                link.add(new Label("numero", Model.of(num)));

                // Solo agregamos la clase 'active-page' si es la actual.
                // La clase base 'btn-pagination' y 'btn-numero' ya están en el HTML.
                if (num.equals(PaginacionPanel.this.paginaActual)) {
                    link.add(new AttributeAppender("class", " active-page"));
                }

                item.add(link);
            }
        });
    }

    private List<Integer> getPaginas() {
        int total = getTotalPaginas();
        if (total == 0) return List.of(1);
        return IntStream.rangeClosed(1, total).boxed().toList();
    }

    private int getTotalPaginas() {
        if (totalResultados == 0) return 1;
        return (int) Math.ceil((double) totalResultados / tamanoPagina);
    }

    private String getTextoPaginacion() {
        if (totalResultados == 0) return "Mostrando 0 registros";
        int desde = (paginaActual - 1) * tamanoPagina + 1;
        int hasta = Math.min(paginaActual * tamanoPagina, totalResultados);
        return "Mostrando " + desde + " a " + hasta + " de " + totalResultados;
    }

    public abstract void onPageChange(int nuevaPagina, AjaxRequestTarget target);

    // Agrégale este método a tu PaginacionPanel
    public void actualizarParametros(int nuevaPagina, int nuevoTotalResultados) {
        this.paginaActual = nuevaPagina;
        this.totalResultados = nuevoTotalResultados;
    }
}