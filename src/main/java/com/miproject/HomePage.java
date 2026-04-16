package com.miproject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.WebMarkupContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends BasePage {

    private FileUploadField fileUploadField;
    private WebMarkupContainer carouselContainer;

    public HomePage() {
        super(); 

        // 1. Mensaje de bienvenida
        add(new Label("mensajeBienvenida", "Bienvenido al Sistema"));

        // 2. Contenedor del carrusel (necesario para actualizarlo por AJAX)
        carouselContainer = new WebMarkupContainer("carouselContainer");
        carouselContainer.setOutputMarkupId(true);
        add(carouselContainer);

        // Feedback panel para mostrar errores o éxitos
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        carouselContainer.add(feedback);

        // 3. Etiqueta con los datos (URLs) de las imágenes para el JS
        Label carouselData = new Label("carouselData", new Model<String>() {
            @Override
            public String getObject() {
                return obtenerRutasImagenes(); // Método que debes implementar
            }
        });
        carouselData.setOutputMarkupId(true);
        carouselData.setEscapeModelStrings(false);
        carouselContainer.add(carouselData);

        // 4. Formulario de subida de imágenes
        Form<Void> uploadForm = new Form<>("uploadForm");
        uploadForm.setMultiPart(true);
        
        fileUploadField = new FileUploadField("imageUpload");
        uploadForm.add(fileUploadField);

        AjaxButton submitButton = new AjaxButton("btnSubirImagen", uploadForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                FileUpload upload = fileUploadField.getFileUpload();
                if (upload != null) {
                    try {
                        String uploadPath = System.getenv("UPLOAD_DIR");
                        if (uploadPath == null) {
                            error("UPLOAD_DIR no configurado.");
                        } else {
                            File newFile = new File(uploadPath, upload.getClientFileName());
                            upload.writeTo(newFile);
                            info("Imagen subida con éxito: " + upload.getClientFileName());
                        }
                    } catch (Exception e) {
                        error("Error al guardar la imagen.");
                    }
                } else {
                    error("Debe seleccionar una imagen.");
                }
                
                // Actualizar todo el contenedor del carrusel y ejecutar el JS
                target.add(carouselContainer);
                target.appendJavaScript("initCarousel();");
            }
        };
        uploadForm.add(submitButton);
        carouselContainer.add(uploadForm);
    }


    private String obtenerRutasImagenes() {
        String uploadPath = System.getenv("UPLOAD_DIR");
        if(uploadPath == null) return "";

        File dir = new File(uploadPath);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        
        if (files == null || files.length == 0) return "";

        List<String> urls = new ArrayList<>();
        for (File f : files) {
            // Usa el ResourceReference que configuraste en WicketApplication
            urls.add("/uploads/carrusel/" + f.getName()); 
        }

        // Devolver como JSON array (asumiendo que tienes una librería como Gson, o puedes hacerlo a mano)
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < urls.size(); i++) {
            sb.append("\"").append(urls.get(i)).append("\"");
            if (i < urls.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}