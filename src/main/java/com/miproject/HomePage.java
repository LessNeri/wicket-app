package com.miproject;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.http.WebRequest;

public class HomePage extends WebPage {
    
    public HomePage() {
        // Hola Mundo
        add(new Label("mensaje", "¡Hola Mundo desde Apache Wicket!"));
        add(new Label("nombre", "Creado por: Leslie"));
        
        // Panel para mensajes
        add(new FeedbackPanel("feedback"));
        
        // Formulario con botón
        Form<Void> form = new Form<Void>("formInsertar") {
            @Override
            protected void onSubmit() {
                try {
                    // Obtener respuesta del captcha
                    WebRequest request = (WebRequest) getRequest();
                    String captchaResponse = request.getRequestParameters()
                        .getParameterValue("h-captcha-response")
                        .toString();
                    
                    // Verificar si el captcha está vacío
                    if (captchaResponse == null || captchaResponse.trim().isEmpty()) {
                        error("Por favor, completa el captcha primero");
                        return;
                    }
                    
                    // Verificar captcha con hCaptcha
                    if (!HCaptchaService.verifyCaptcha(captchaResponse)) {
                        error("Captcha incorrecto o expirado. Intenta de nuevo");
                        return;
                    }
                    
                    // Si captcha es válido, insertar datos
                    String nombre = "Ana";
                    String apellido = "García";
                    
                    DatabaseManager.insertarUsuario(nombre, apellido);
                    
                    // Mensaje de confirmación
                    success("Datos insertados correctamente en la base de datos");
                    
                } catch (Exception e) {
                    error("Error al procesar la solicitud: " + e.getMessage());
                }
            }
        };
        
        add(form);
        form.add(new Button("btnInsertar"));
    }
}