package com.miproject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.miproject.pages.LoginPage;
import com.miproject.pages.principal.ClientePage;
import com.miproject.pages.seguridad.ModuloPage;
import com.miproject.pages.seguridad.PerfilPage;
import com.miproject.pages.seguridad.PermisoPage;
import com.miproject.pages.seguridad.UsuarioPage;

import com.miproject.models.Perfil;
import com.miproject.models.PermisoPerfil;
import com.miproject.models.Modulo;
import com.miproject.dao.PerfilDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.dao.ModuloDAO;
import com.miproject.services.JWTService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasePage extends WebPage {

    public BasePage() {
        int idPerfilActual = obtenerIdPerfilDesdeToken();
        PerfilDAO perfilDAO = new PerfilDAO();
        PermisoDAO permisoDAO = new PermisoDAO();
        
        Perfil perfilUsuario = perfilDAO.obtenerPorId(idPerfilActual);
        
        List<PermisoPerfil> permisosLista = permisoDAO.obtenerPorPerfil(idPerfilActual);
        Map<Integer, PermisoPerfil> mapaPermisos = new HashMap<>();
        for (PermisoPerfil p : permisosLista) {
            mapaPermisos.put(p.getIdModulo(), p);
        }
        
        ModuloDAO moduloDAO = new ModuloDAO();

        List<Modulo> menusPrincipales = moduloDAO.obtenerMenusPrincipales();

        ListView<Modulo> listaMenus = new ListView<Modulo>("listaMenus", menusPrincipales) {
            @Override
            protected void populateItem(ListItem<Modulo> itemPadre) {
                Modulo menuPadre = itemPadre.getModelObject();
                
                if (!tienePermiso(mapaPermisos, perfilUsuario, menuPadre.getId())) {
                    itemPadre.setVisible(false);
                    return;
                }

                WebMarkupContainer iconoPadre = new WebMarkupContainer("iconoMenuPadre");
                iconoPadre.add(new AttributeModifier("class", obtenerIcono(menuPadre.getId(), true)));
                itemPadre.add(iconoPadre);

                itemPadre.add(new Label("nombreMenuPadre", menuPadre.getStrNombreModulo()));

                List<Modulo> submenusDB = moduloDAO.obtenerHijosPorPadre(menuPadre.getId());
                
                ListView<Modulo> listaSubmenus = new ListView<Modulo>("listaSubmenus", submenusDB) {
                    @Override
                    protected void populateItem(ListItem<Modulo> itemHijo) {
                        Modulo submenu = itemHijo.getModelObject();
                        
                        if (!tienePermiso(mapaPermisos, perfilUsuario, submenu.getId())) {
                            itemHijo.setVisible(false);
                            return;
                        }

                        Link<Void> linkSubmenu = new Link<Void>("linkSubmenu") {
                            @Override
                            public void onClick() {
                                irAPagina(submenu.getId());
                            }
                        };
                        
                        // === NUEVO: ASIGNAR ÍCONO AL SUBMENÚ ===
                        WebMarkupContainer iconoHijo = new WebMarkupContainer("iconoSubmenu");
                        iconoHijo.add(new AttributeModifier("class", obtenerIcono(submenu.getId(), false)));
                        linkSubmenu.add(iconoHijo);

                        linkSubmenu.add(new Label("nombreSubmenu", submenu.getStrNombreModulo()));
                        itemHijo.add(linkSubmenu);
                    }
                };
                
                itemPadre.add(listaSubmenus);
            }
        };
        add(listaMenus);

        add(new Link<Void>("logout") {
            @Override
            public void onClick() {
                javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("jwt_token", "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                getWebResponse().addCookie(cookie);
                setResponsePage(LoginPage.class);
            }
        });
    }

    private void irAPagina(int idModuloDB) {
        switch(idModuloDB) {
            case 1:
                setResponsePage(PerfilPage.class);
                break;
            case 2:
                setResponsePage(ModuloPage.class);
                break;
            case 3:
                setResponsePage(PermisoPage.class);
                break;
            case 4:
                setResponsePage(UsuarioPage.class);
                break;
            case 5:
                setResponsePage(ClientePage.class, new PageParameters().add("origen", 5));
                break;
            case 6:
                setResponsePage(ClientePage.class, new PageParameters().add("origen", 6));
                break;
            case 7:
                setResponsePage(ClientePage.class, new PageParameters().add("origen", 7));
                break;
            case 8:
                setResponsePage(ClientePage.class, new PageParameters().add("origen", 8));
                break;
            // Cuando crees nuevos módulos, solo agrega los "cases" aquí
            default:
                setResponsePage(HomePage.class); 
                break;
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        List<BreadcrumbItem> breadcrumbsList = getBreadcrumbs();
        
        add(new ListView<BreadcrumbItem>("breadcrumbs", breadcrumbsList) {
            @Override
            protected void populateItem(ListItem<BreadcrumbItem> item) {
                BreadcrumbItem bcItem = item.getModelObject();
                boolean isLast = (item.getIndex() == getList().size() - 1);
                
                BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", bcItem.getPageClass(), bcItem.getParameters()) {
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setEnabled(!isLast); 
                    }
                };
                
                link.setAutoEnable(false); 
                link.add(new Label("label", bcItem.getLabel() != null ? bcItem.getLabel() : ""));
                item.add(link);
            }
        });
    }

    private boolean tienePermiso(Map<Integer, PermisoPerfil> mapa, Perfil perfil, int idModulo) {
        if (perfil != null && perfil.isBitAdministrador()) {
            return true; 
        }
        PermisoPerfil permiso = mapa.get(idModulo);
        return permiso != null && permiso.isBitConsulta();
    }

    private int obtenerIdPerfilDesdeToken() {
        HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    Integer idPerfil = JWTService.getPerfilIdFromToken(cookie.getValue());
                    return idPerfil != null ? idPerfil : -1;
                }
            }
        }
        return -1;
    }

    private String obtenerIcono(int idModuloDB, boolean esPadre) {
        switch(idModuloDB) {
            case 10: return "fas fa-shield-alt";     
            case 20: return "fas fa-tachometer-alt";
            case 30: return "fas fa-cogs"; 
            
            case 1: return "fas fa-id-badge";    // Perfil
            case 2: return "fas fa-cubes";       // Modulo
            case 3: return "fas fa-key";         // Permiso
            case 4: return "fas fa-users";       // Usuario
            case 5: return "fas fa-user-tie";    // Cliente (Origen 5)
            case 6: return "fas fa-building";    // Cliente (Origen 6)
            case 7: return "fas fa-truck";       // Cliente (Origen 7)
            case 8: return "fas fa-store";       // Cliente (Origen 8)
            
            default:
                return esPadre ? "fas fa-folder" : "fas fa-circle-notch";
        }
    }

    protected List<BreadcrumbItem> getBreadcrumbs() {
        List<BreadcrumbItem> list = new ArrayList<>();
        list.add(new BreadcrumbItem("Inicio", HomePage.class));
        return list;
    }

    public static class BreadcrumbItem implements java.io.Serializable {
        private String label;
        private Class<? extends WebPage> pageClass;
        private PageParameters parameters;

        public BreadcrumbItem(String label, Class<? extends WebPage> pageClass) {
            this.label = label;
            this.pageClass = pageClass;
            this.parameters = new PageParameters();
        }

        public BreadcrumbItem(String label, Class<? extends WebPage> pageClass, PageParameters parameters) {
            this.label = label;
            this.pageClass = pageClass;
            this.parameters = parameters != null ? parameters : new PageParameters();
        }
        
        public String getLabel() { return label; }
        public Class<? extends WebPage> getPageClass() { return pageClass; }
        public PageParameters getParameters() { return parameters; }
    }
}