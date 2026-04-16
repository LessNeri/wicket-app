package com.miproject.pages;

import org.apache.wicket.markup.html.WebPage;
import com.miproject.services.AuthService;
import com.miproject.services.JWTService;
import com.miproject.models.Usuario;
import com.miproject.HCaptchaService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import javax.servlet.http.Cookie;

public class LoginPage extends WebPage {

    private EmailTextField emailField;
    private PasswordTextField passwordField;
    private HiddenField<String> captchaToken;
    private Label mensajeExito;
    private FeedbackPanel feedback;

    public LoginPage() {

        add(new Label("titulo", "Iniciar Sesión"));

        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        mensajeExito = new Label("mensajeExito", "¡Login exitoso! Redirigiendo...");
        mensajeExito.setOutputMarkupId(true);
        mensajeExito.setVisible(false);
        add(mensajeExito);

        Form<Void> form = new Form<>("loginForm");

        emailField = new EmailTextField("email", Model.of(""));
        emailField.setRequired(true);
        emailField.add(EmailAddressValidator.getInstance());
        emailField.add(StringValidator.maximumLength(100));
        emailField.setLabel(Model.of("Correo electrónico"));
        form.add(emailField);

        passwordField = new PasswordTextField("password", Model.of(""));
        passwordField.setRequired(true);
        passwordField.setLabel(Model.of("Contraseña"));
        form.add(passwordField);

        captchaToken = new HiddenField<>("captchaToken", Model.of(""));
        captchaToken.setMarkupId("captchaTokenField");
        captchaToken.setOutputMarkupId(true);
        form.add(captchaToken);

        captchaToken.add(new org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        
        form.add(new org.apache.wicket.markup.html.WebMarkupContainer("captchaContainer"));

        AjaxButton btnLogin = new AjaxButton("btnLogin") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                String email = emailField.getModelObject();
                String password = passwordField.getModelObject();
                String captcha = captchaToken.getModelObject();

                if (captcha == null || captcha.trim().isEmpty()) {
                    error("Por favor, confirme que es humano resolviendo el captcha.");
                    target.add(feedback);
                    return;
                }
                if (!HCaptchaService.verifyCaptcha(captcha)) {
                    error("Validación de hCaptcha fallida. Intente nuevamente.");
                    target.add(feedback);
                    // Opcional: limpiar el campo para forzar nuevo captcha
                    captchaToken.setModelObject("");
                    target.add(captchaToken);
                    return;
                }

                try {
                    Usuario usuario = AuthService.validarLogin(email, password);

                    String token = JWTService.generarToken(
                            usuario.getId(),
                            usuario.getStrNombreUsuario(),
                            usuario.getIdPerfil());

                    WebResponse response = (WebResponse) getResponse();
                    Cookie cookie = new Cookie("jwt_token", token);
                    cookie.setMaxAge(24 * 60 * 60);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);

                    mensajeExito.setVisible(true);
                    target.add(mensajeExito);
                    target.appendJavaScript("setTimeout(function() { window.location.href = '/home'; }, 1500);");

                } catch (Exception e) {
                    error(e.getMessage());
                    target.add(feedback);

                    captchaToken.setModelObject("");
                    target.add(captchaToken);
                    target.appendJavaScript("hcaptcha.reset();");
                }
            }
        };

        form.add(btnLogin);
        add(form);
    }
}