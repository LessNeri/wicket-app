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
            
            String action = getRequest().getQueryParameters().getParameterValue("action").toString();

            if ("api".equals(action)) {
                procesarPeticionApi();
            }
        }

        private void procesarPeticionApi() {
        String metodo = getRequest().getQueryParameters().getParameterValue("metodo").toString();

        String search = getRequest().getQueryParameters().getParameterValue("search").toString();
        String fechaDesde = getRequest().getQueryParameters().getParameterValue("fechaDesde").toString();
        String fechaHasta = getRequest().getQueryParameters().getParameterValue("fechaHasta").toString();
        String paginaStr = getRequest().getQueryParameters().getParameterValue("pagina").toString();
        String tamanoStr = getRequest().getQueryParameters().getParameterValue("tamano").toString();

        int pagina = 1;
        int tamano = 10;

        if (paginaStr != null && !paginaStr.isEmpty()) {
            pagina = Integer.parseInt(paginaStr);
        }
        if (tamanoStr != null && !tamanoStr.isEmpty()) {
            tamano = Integer.parseInt(tamanoStr);
        }

        String jsonRespuesta = "";

        try {
            if ("listar".equals(metodo)) {
    List<Usuario> lista = usuarioDAO.buscarConFiltros(search, fechaDesde, fechaHasta, pagina, tamano);
    int total = usuarioDAO.contarConFiltros(search, fechaDesde, fechaHasta);
    
    // Crear un objeto con los resultados y el total
    String jsonResultado = String.format(
        "{\"usuarios\":%s,\"total\":%d}",
        jackson.writeValueAsString(lista),
        total
    );
    
    jsonRespuesta = jsonResultado;
} else {
                // El resto del código (crear, actualizar, eliminar) se queda igual
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
                    jsonRespuesta = exito ? "{\"status\":\"ok\"}" : "\"status\":\"error\"}";

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

        TextRequestHandler jsonHandler = new TextRequestHandler("application/json", "UTF-8", jsonRespuesta);
        RequestCycle.get().replaceAllRequestHandlers(jsonHandler);
    }

    }