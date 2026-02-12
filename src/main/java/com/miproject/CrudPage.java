package com.miproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.List;

public class CrudPage extends WebPage {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ObjectMapper jackson = new ObjectMapper();

    public CrudPage(final PageParameters parameters) {
        super(parameters);
        
        // Verificamos si la petición viene del Fetch API (buscamos el parámetro ?action=api)
        String action = getRequest().getQueryParameters().getParameterValue("action").toString();

        if ("api".equals(action)) {
            procesarPeticionApi();
        }
        // Si no es API, Wicket renderizará el HTML automáticamente (CrudPage.html)
    }

    private void procesarPeticionApi() {
        // Obtenemos parámetros
        String metodo = getRequest().getQueryParameters().getParameterValue("metodo").toString();
        String jsonRespuesta = "";

        try {
            if ("listar".equals(metodo)) {
                // LEER
                List<Usuario> lista = usuarioDAO.leerTodos();
                jsonRespuesta = jackson.writeValueAsString(lista);

            } else {
                // LEER EL BODY
                HttpServletRequest servletRequest = (HttpServletRequest) getRequest().getContainerRequest();
                BufferedReader reader = servletRequest.getReader();
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String jsonBody = sb.toString();

                if ("crear".equals(metodo)) {
                    Usuario nuevo = jackson.readValue(jsonBody, Usuario.class);
                    boolean exito = usuarioDAO.crearUsuario(nuevo);
                    jsonRespuesta = exito ? "{\"status\":\"ok\"}" : "{\"status\":\"error\"}";

                } else if ("actualizar".equals(metodo)) {
                    Usuario editar = jackson.readValue(jsonBody, Usuario.class);
                    boolean exito = usuarioDAO.actualizarUsuario(editar);
                    jsonRespuesta = exito ? "{\"status\":\"ok\"}" : "{\"status\":\"error\"}";

                } else if ("eliminar".equals(metodo)) {
                    Usuario aBorrar = jackson.readValue(jsonBody, Usuario.class);
                    boolean exito = usuarioDAO.eliminarUsuario(aBorrar.getId());
                    jsonRespuesta = exito ? "{\"status\":\"ok\"}" : "{\"status\":\"error\"}";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta = "{\"error\": \"" + e.getMessage() + "\"}";
        }

        // === CORRECCIÓN DEFINITIVA ===
        // Creamos el manejador que contiene el JSON
        TextRequestHandler jsonHandler = new TextRequestHandler("application/json", "UTF-8", jsonRespuesta);

        // Le decimos al ciclo de petición (RequestCycle) que olvide todo lo demás (el HTML)
        // y solamente ejecute nuestro jsonHandler.
        RequestCycle.get().replaceAllRequestHandlers(jsonHandler);
    }
}