package com.miproject;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.util.lang.Bytes;

public class HomePage extends WebPage {
    
    private Label carouselData;

    public HomePage() {
        add(new Label("mensaje", "¡Hola Mundo desde Apache Wicket!"));
        add(new Label("nombre", "Creado por: Leslie"));

        WebMarkupContainer carouselContainer = new WebMarkupContainer("carouselContainer");
        carouselContainer.setOutputMarkupId(true);
        add(carouselContainer);

        List<String> images = ImageManager.getCarruselImages(true);
        
        carouselData = new Label("carouselData", String.join(",", images));
        carouselData.setEscapeModelStrings(false);
        carouselData.setOutputMarkupId(true);
        carouselContainer.add(carouselData);

        Form<Void> uploadForm = new Form<>("uploadForm");
        uploadForm.setMultiPart(true);
        uploadForm.setMaxSize(Bytes.megabytes(5));
        add(uploadForm);

        FileUploadField uploadField = new FileUploadField("imageUpload");
        uploadForm.add(uploadField);

        uploadForm.add(new AjaxButton("btnSubirImagen", uploadForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                FileUpload upload = uploadField.getFileUpload();

                if (upload == null) {
                    error("Selecciona una imagen");
                } else if (ImageManager.saveUploadedImage(upload)) {
                    List<String> nuevasImagenes = ImageManager.getCarruselImages(true);
                    carouselData.setDefaultModelObject(String.join(",", nuevasImagenes));
                    success("Imagen subida correctamente");
                    target.add(carouselData);
                    target.appendJavaScript("initCarousel();");
                } else {
                    error("Archivo no válido");
                }
                target.add(getPage().get("feedback"));
            }
        });

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