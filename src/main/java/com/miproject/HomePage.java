package com.miproject;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class HomePage extends BasePage {

    private Label carouselData;
    private HiddenField<String> captchaToken;
    private Button btnIrInsercionDatos;

    public HomePage() {

        super(List.of(
            new BreadcrumbItem("Inicio", HomePage.class)
        ));

        /* ====== MENSAJES ====== */
        add(new Label("mensaje", "¡Hola Mundo desde Apache Wicket!"));
        add(new Label("nombre", "Creado por: Leslie"));

        /* ====== CARRUSEL (NO SE TOCA) ====== */
        WebMarkupContainer carouselContainer =
                new WebMarkupContainer("carouselContainer");
        carouselContainer.setOutputMarkupId(true);
        add(carouselContainer);

        List<String> images = ImageManager.getCarruselImages(true);

        carouselData = new Label(
                "carouselData",
                String.join(",", images)
        );
        carouselData.setEscapeModelStrings(false);
        carouselData.setOutputMarkupId(true);
        carouselContainer.add(carouselData);

        /* ====== SUBIR IMAGEN (NO SE TOCA) ====== */
        Form<Void> uploadForm = new Form<>("uploadForm");
        uploadForm.setMultiPart(true);
        uploadForm.setMaxSize(Bytes.megabytes(5));
        add(uploadForm);

        FileUploadField uploadField =
                new FileUploadField("imageUpload");
        uploadForm.add(uploadField);

        uploadForm.add(new AjaxButton("btnSubirImagen", uploadForm) {
    @Override
    protected void onSubmit(AjaxRequestTarget target) {

        FileUpload upload = uploadField.getFileUpload();

        // ===== VALIDAR QUE HAYA ARCHIVO =====
        if (upload == null) {
            error("Selecciona una imagen");
            target.add(getPage().get("feedback"));
            return;
        }

        // ===== VALIDAR TIPO DE ARCHIVO (SOLO IMÁGENES) =====
        String contentType = upload.getContentType();

        boolean esImagen =
                contentType != null && (
                    contentType.equals("image/jpeg") ||
                    contentType.equals("image/png") ||
                    contentType.equals("image/gif") ||
                    contentType.equals("image/webp")
                );

        if (!esImagen) {
            error("Solo se permiten imágenes (JPG, PNG, GIF, WEBP)");
            target.add(getPage().get("feedback"));
            return;
        }

        // ===== GUARDAR IMAGEN =====
        if (ImageManager.saveUploadedImage(upload)) {

            List<String> nuevasImagenes =
                    ImageManager.getCarruselImages(true);

            carouselData.setDefaultModelObject(
                    String.join(",", nuevasImagenes)
            );

            success("Imagen subida correctamente");
            target.add(carouselData);
            target.appendJavaScript("initCarousel();");

        } else {
            error(" Error al guardar la imagen");
        }

        target.add(getPage().get("feedback"));
    }
});


        /* ====== FEEDBACK ====== */
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        /* ====== FORM CAPTCHA ====== */
        Form<Void> formCaptcha = new Form<>("formCaptcha") {
            @Override
            protected void onSubmit() {
                // Validar captcha
                String token = captchaToken.getModelObject();
                
                if (token == null || token.isEmpty()) {
                    error("Por favor, complete el captcha");
                    return;
                }
                
                if (HCaptchaService.verifyCaptcha(token)) {
                    // Navegar a InsertarDatosPage
                    setResponsePage(InsertarDatosPage.class);
                } else {
                    error("Captcha no válido. Intente nuevamente.");
                }
            }
        };
        add(formCaptcha);

        captchaToken = new HiddenField<>("captchaToken", Model.of(""));
        captchaToken.setOutputMarkupId(true);
        formCaptcha.add(captchaToken);

        WebMarkupContainer captchaContainer =
                new WebMarkupContainer("captchaContainer");
        captchaContainer.setOutputMarkupId(true);
        formCaptcha.add(captchaContainer);

        btnIrInsercionDatos = new Button("btnIrInsercionDatos") {
    @Override
    public void onSubmit() {
        System.out.println("=== BOTÓN IR A INSERCIÓN DE DATOS CLICKEADO ===");
        setResponsePage(InsertarDatosPage.class);
    }
};
        btnIrInsercionDatos.setOutputMarkupId(true);
        formCaptcha.add(btnIrInsercionDatos);

add(new BookmarkablePageLink<Void>("linkGestionUsuarios", GestionUsuariosPage.class));
    }
}