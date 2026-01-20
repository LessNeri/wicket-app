package com.miproject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.link.Link;


public class HomePage extends WebPage {

    public HomePage() {

        add(new Label("mensaje", "¡Hola Mundo desde Apache Wicket!"));
        add(new Label("nombre", "Creado por: Leslie"));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Form<Void> form = new Form<>("formInsertar") {
            @Override
            protected void onSubmit() {
                DatabaseManager.insertarUsuario("Ana", "García");
                success("Datos insertados correctamente");
            }
        };
        add(form);

        WebMarkupContainer captchaContainer = new WebMarkupContainer("captchaContainer");
captchaContainer.setOutputMarkupId(true);
form.add(captchaContainer);


        AjaxButton btnCaptcha = new AjaxButton("btnCaptcha", form) {
    @Override
    protected void onSubmit(AjaxRequestTarget target) {
        target.appendJavaScript(
            "document.getElementById('" 
            + captchaContainer.getMarkupId() 
            + "').style.display='block';"
        );
    }
};
btnCaptcha.setDefaultFormProcessing(false);
form.add(btnCaptcha);


        form.add(new Button("btnInsertar"));


form.add(new Link<Void>("btnIrFormulario") {
    @Override
    public void onClick() {
        setResponsePage(FormularioPage.class);
    }
});



    }
}