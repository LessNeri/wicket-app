package com.miproject;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class UsuariosApiPage extends WebPage {

    public UsuariosApiPage() {
        // 1. Obtener la lista de usuarios de la BD
        UsuarioDAO dao = new UsuarioDAO();
        
        // CORRECCIÓN AQUÍ: Usamos 'leerTodos()' que es como se llama en tu DAO
        List<Usuario> lista = dao.leerTodos(); 

        // 2. Convertir esa lista a texto JSON (String)
        String jsonResultado = "[]";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonResultado = mapper.writeValueAsString(lista);
        } catch (Exception e) {
            e.printStackTrace();
            jsonResultado = "[{\"error\": \"Error al convertir datos\"}]";
        }

        // 3. Responder al navegador con JSON puro
        getRequestCycle().scheduleRequestHandlerAfterCurrent(
            new TextRequestHandler("application/json", "UTF-8", jsonResultado)
        );
    }
}