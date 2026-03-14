package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Evento;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.UsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    private static final UsuarioService usuarioService = new UsuarioService();
    private static final EventoService eventoService = new EventoService();

    public static void listarUsuarios(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Usuario> usuarios = usuarioService.findAll();
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("usuarios", usuarios);
        model.put("roles", RolUsuario.values());
        model.put("pageTitle", "Gestión de Usuarios");
        model.put("currentPage", "admin-usuarios");
        ctx.render("admin/usuarios.html", model);
    }

    public static void toggleBloquear(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        try {
            Usuario u = usuarioService.bloquearUsuario(id);
            ctx.json(Map.of("success", true, "activo", u.isActivo()));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void cambiarRol(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        String rolStr = ctx.formParam("rol");
        if (rolStr == null) {
            ctx.json(Map.of("success", false, "message", "Rol no especificado"));
            return;
        }
        try {
            RolUsuario nuevoRol = RolUsuario.valueOf(rolStr);
            Usuario u = usuarioService.cambiarRol(id, nuevoRol);
            ctx.json(Map.of("success", true, "rol", u.getRol().name()));
        } catch (IllegalArgumentException e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void listarEventos(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Evento> eventos = eventoService.findAllOrdered();
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("eventos", eventos);
        model.put("pageTitle", "Gestión de Eventos");
        model.put("currentPage", "admin-eventos");
        ctx.render("admin/eventos.html", model);
    }

    public static void eliminarEvento(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        try {
            eventoService.delete(id);
            ctx.json(Map.of("success", true));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
