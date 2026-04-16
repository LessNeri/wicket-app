package com.miproject.services;

import com.miproject.dao.MenuDAO;
import com.miproject.dao.ModuloDAO;
import com.miproject.dao.PermisoDAO;
import com.miproject.models.Menu;
import com.miproject.models.Modulo;
import com.miproject.models.PermisoPerfil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuService {

    private static MenuDAO menuDAO = new MenuDAO();
    private static ModuloDAO moduloDAO = new ModuloDAO();
    private static PermisoDAO permisoDAO = new PermisoDAO();

    public static Map<String, List<Modulo>> obtenerMenuPorPerfil(int idPerfil) {
        Map<String, List<Modulo>> menuCompleto = new HashMap<>();
        
        List<PermisoPerfil> permisos = permisoDAO.obtenerPorPerfil(idPerfil);
        
        Map<Integer, PermisoPerfil> permisosMap = new HashMap<>();
        for (PermisoPerfil p : permisos) {
            permisosMap.put(p.getIdModulo(), p);
        }

        List<Menu> menusPrincipales = menuDAO.obtenerMenusPrincipales();
        
        for (Menu menuPrincipal : menusPrincipales) {
            Modulo moduloPrincipal = moduloDAO.obtenerPorId(menuPrincipal.getIdModulo());
            
            // Evaluamos si el menú principal tiene algún submenú válido
            List<Modulo> submenus = new ArrayList<>();
            List<Menu> submenusMenu = menuDAO.obtenerSubmenus(menuPrincipal.getIdModulo());
            
            for (Menu submenu : submenusMenu) {
                Modulo moduloSubmenu = moduloDAO.obtenerPorId(submenu.getIdModulo());
                
                // LA REGLA DE ORO: Solo se muestra si tiene permiso de CONSULTAR
                if (tienePermisoConsultar(permisosMap, submenu.getIdModulo())) {
                    submenus.add(moduloSubmenu);
                }
            }
            
            // Solo agregamos el menú principal si tiene al menos un submenú visible
            if (!submenus.isEmpty()) {
                menuCompleto.put(moduloPrincipal.getStrNombreModulo(), submenus);
            }
        }
        
        return menuCompleto;
    }

    // Método corregido para aplicar la Regla de Oro
    private static boolean tienePermisoConsultar(Map<Integer, PermisoPerfil> permisosMap, int idModulo) {
        PermisoPerfil p = permisosMap.get(idModulo);
        if (p == null) return false;
        
        return p.isBitConsulta(); 
    }

    public static PermisoPerfil obtenerPermisosModulo(int idPerfil, int idModulo) {
        List<PermisoPerfil> permisos = permisoDAO.obtenerPorPerfil(idPerfil);
        for (PermisoPerfil p : permisos) {
            if (p.getIdModulo() == idModulo) {
                return p;
            }
        }
        return null;
    }
}