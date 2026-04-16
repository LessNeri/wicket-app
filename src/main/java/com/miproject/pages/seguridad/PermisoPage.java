package com.miproject.pages.seguridad;

import com.miproject.BasePage;
import com.miproject.BreadcrumbItem;
import com.miproject.HomePage;
import com.miproject.models.Perfil;
import com.miproject.models.Modulo;
import com.miproject.models.PermisoPerfil;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.ModuloDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.services.JWTService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

import javax.servlet.http.Cookie;
import java.util.*;

public class PermisoPage extends BasePage {

    private PerfilDAO perfilDAO = new PerfilDAO();
    private ModuloDAO moduloDAO = new ModuloDAO();
    private PermisoDAO permisoDAO = new PermisoDAO();

    private Perfil perfilSeleccionado;
    private List<Perfil> perfiles;
    private Map<Integer, PermisoPerfil> permisosMap = new HashMap<>();
    
    private boolean esAdmin;
    private boolean cambiosPendientes = false; 
    private AjaxButton btnGuardar;

    private org.apache.wicket.markup.html.panel.FeedbackPanel feedbackPanel;

    public PermisoPage() {
        super();

        // 1. Lógica de Seguridad del Usuario Logueado
        int idPerfilUsuario = obtenerIdPerfilDesdeToken();
        Perfil perfilUsuario = perfilDAO.obtenerPorId(idPerfilUsuario);
        esAdmin = perfilUsuario != null && perfilUsuario.isBitAdministrador();

        perfiles = perfilDAO.listarTodos();

        // ELIMINAMOS la auto-selección. perfilSeleccionado arranca como null.

        // 2. Componentes fuera del Form
        add(new Label("titulo", "Gestión de Permisos por Perfil"));

        Label mensajeSoloLectura = new Label("mensajeSoloLectura", "Modo solo lectura. No tienes permisos para editar, contacta a un administrador.");
        mensajeSoloLectura.setVisible(!esAdmin);
        add(mensajeSoloLectura);

        Form<Void> form = new Form<Void>("formularioPermisos") {
        @Override
        protected void onConfigure() {
            super.onConfigure();
            setVisible(perfilSeleccionado != null);
        }
        };
    form.setOutputMarkupPlaceholderTag(true);

        feedbackPanel = new org.apache.wicket.markup.html.panel.FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true); 
        form.add(feedbackPanel);

        // Selector de Perfil
        DropDownChoice<Perfil> perfilSelector = new DropDownChoice<>("perfilSelector",
                new PropertyModel<>(this, "perfilSeleccionado"), 
                perfiles,
                new ChoiceRenderer<>("strNombrePerfil", "id"));
        
        perfilSelector.setNullValid(true); // Permite mostrar "Seleccione Uno..." por defecto

        perfilSelector.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                cargarPermisos();
                cambiosPendientes = false; 
                target.add(form); // Actualizamos todo el formulario para que se muestre/oculte
            }
        });
        add(perfilSelector);

        // 4. Jerarquía de Módulos dentro del Formulario
        List<Modulo> padres = moduloDAO.obtenerPadres(); 

        form.add(new ListView<Modulo>("grupoMenu", padres) {
            @Override
            protected void populateItem(ListItem<Modulo> parentItem) {
                Modulo padre = parentItem.getModelObject();
                parentItem.add(new Label("nombrePadre", padre.getStrNombreModulo().toUpperCase()));

                List<Modulo> hijos = moduloDAO.obtenerHijosPorPadre(padre.getId());

                parentItem.add(new ListView<Modulo>("moduloList", hijos) {
                    @Override
                    protected void populateItem(ListItem<Modulo> item) {
                        Modulo modulo = item.getModelObject();
                        PermisoPerfil permiso = permisosMap.get(modulo.getId());

                        // Lógica de validación del Perfil Seleccionado
                        boolean esPerfilSeleccionadoAdmin = perfilSeleccionado != null && perfilSeleccionado.isBitAdministrador();
                        
                        // Los checks se habilitan SI el usuario logueado es admin Y el perfil seleccionado NO es admin
                        boolean checksHabilitados = esAdmin && !esPerfilSeleccionadoAdmin;

                        if (permiso == null) {
                            permiso = new PermisoPerfil();
                            permiso.setIdModulo(modulo.getId());
                            permiso.setIdPerfil(perfilSeleccionado.getId());
                            permisosMap.put(modulo.getId(), permiso); 
                        }

                        // SI el perfil seleccionado es ADMIN, forzamos todos los permisos a TRUE irrevocablemente
                        if (esPerfilSeleccionadoAdmin) {
                            permiso.setBitAgregar(true);
                            permiso.setBitEditar(true);
                            permiso.setBitEliminar(true);
                            permiso.setBitConsulta(true);
                        }

                        item.add(new Label("nombreModulo", modulo.getStrNombreModulo()));
                        
                        // Checkboxes
                        item.add(createCheckbox("agregar", permiso, "bitAgregar", checksHabilitados));
                        item.add(createCheckbox("editar", permiso, "bitEditar", checksHabilitados));
                        item.add(createCheckbox("eliminar", permiso, "bitEliminar", checksHabilitados));
                        item.add(createCheckbox("consultar", permiso, "bitConsulta", checksHabilitados));
                        
                        // Botón de Basura
                        item.add(new AjaxLink<Void>("limpiarPermisos") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                PermisoPerfil p = permisosMap.get(modulo.getId());
                                if (p != null) {
                                    p.setBitAgregar(false);
                                    p.setBitEditar(false);
                                    p.setBitEliminar(false);
                                    p.setBitConsulta(false);
                                }
                                cambiosPendientes = true;
                                target.add(form); 
                            }
                        }.setVisible(checksHabilitados)); // Se oculta la basura si es perfil admin
                    }
                });
            }
        });

// Botón de Guardado
        btnGuardar = new AjaxButton("btnGuardar", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                guardarPermisos();
                cambiosPendientes = false; 
                target.add(this); 
                
                // CRÍTICO: Refrescamos el panel de mensajes para que aparezca el éxito o error
                target.add(feedbackPanel); 
            }
            
            @Override
            protected void onConfigure() {
                super.onConfigure();
                boolean esPerfilSeleccionadoAdmin = perfilSeleccionado != null && perfilSeleccionado.isBitAdministrador();
                setEnabled(esAdmin && cambiosPendientes && !esPerfilSeleccionadoAdmin);
                setVisible(esAdmin && !esPerfilSeleccionadoAdmin);
            }
        };
        btnGuardar.setOutputMarkupId(true);
        form.add(btnGuardar);

        add(form);
        
    }

    private void cargarPermisos() {
        if (perfilSeleccionado != null) {
            List<PermisoPerfil> permisos = permisoDAO.obtenerPorPerfil(perfilSeleccionado.getId());
            permisosMap = new HashMap<>();
            for (PermisoPerfil p : permisos) {
                permisosMap.put(p.getIdModulo(), p);
            }
        }
    }

    private void guardarPermisos() {
        if (!esAdmin) {
            error("No tienes permisos para realizar esta acción.");
            return;
        }
        
        List<PermisoPerfil> listaParaGuardar = new ArrayList<>(permisosMap.values());
        if (listaParaGuardar.isEmpty()) {
            info("No hay cambios que guardar.");
            return;
        }

        boolean exito = permisoDAO.guardarPermisos(listaParaGuardar);
        
        if (exito) {
            success("¡Permisos guardados correctamente!"); 
        } else {
            error("Ocurrió un error al guardar en la base de datos.");
        }
    }

    private CheckBox createCheckbox(String id, PermisoPerfil permiso, String property, boolean enabled) {
        CheckBox check = new CheckBox(id, new PropertyModel<>(permiso, property));
        check.setEnabled(enabled);
        
        if (enabled) {
            check.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    cambiosPendientes = true; 
                    target.add(btnGuardar); 
                }
            });
        }
        return check;
    }

    private int obtenerIdPerfilDesdeToken() {
        Cookie[] cookies = ((javax.servlet.http.HttpServletRequest) getRequest().getContainerRequest()).getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    Integer idPerfil = JWTService.getPerfilIdFromToken(cookie.getValue());
                    return idPerfil != null ? idPerfil : 1;
                }
            }
        }
        return 1; // Default
    }

    @Override
    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = super.getBreadcrumbs();
        list.add(new BreadcrumbItem("Seguridad", HomePage.class));
        list.add(new BreadcrumbItem("Permisos-Perfil", PermisoPage.class));
        return list;
    }
}