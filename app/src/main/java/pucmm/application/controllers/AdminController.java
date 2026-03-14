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
        List<Usuario> usuarios = usuarioService.findAllWithCreadoPor();
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

    public static void crearUsuarioPage(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("roles", RolUsuario.values());
        model.put("pageTitle", "Crear Usuario");
        model.put("currentPage", "admin-usuarios");
        String error = ctx.queryParam("error");
        if (error != null) model.put("error", error);
        String success = ctx.queryParam("success");
        if (success != null) model.put("success", success);
        ctx.render("admin/crear-usuario.html", model);
    }

    public static void crearUsuario(Context ctx) {
        Usuario admin = ctx.attribute("usuario");
        String nombre = ctx.formParam("nombre");
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String rolStr = ctx.formParam("rol");

        if (nombre == null || nombre.isBlank() || username == null || username.isBlank()
                || password == null || password.isBlank() || rolStr == null) {
            ctx.redirect("/admin/usuarios/crear?error=Todos+los+campos+son+requeridos");
            return;
        }

        if (password.length() < 4) {
            ctx.redirect("/admin/usuarios/crear?error=La+contraseña+debe+tener+al+menos+4+caracteres");
            return;
        }

        try {
            RolUsuario rol = RolUsuario.valueOf(rolStr);
            usuarioService.registrarPorAdmin(nombre.trim(), username.trim(), password, rol, admin);
            ctx.redirect("/admin/usuarios/crear?success=Usuario+creado+exitosamente");
        } catch (IllegalArgumentException e) {
            ctx.redirect("/admin/usuarios/crear?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public static void editarUsuarioPage(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Usuario target = usuarioService.find(id);

        if (target == null) {
            ctx.redirect("/admin/usuarios");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("target", target);
        model.put("roles", RolUsuario.values());
        model.put("pageTitle", "Editar Usuario");
        model.put("currentPage", "admin-usuarios");
        String error = ctx.queryParam("error");
        if (error != null) model.put("error", error);
        ctx.render("admin/editar-usuario.html", model);
    }

    public static void editarUsuario(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        String nombre = ctx.formParam("nombre");
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String rolStr = ctx.formParam("rol");

        if (nombre == null || nombre.isBlank() || username == null || username.isBlank() || rolStr == null) {
            ctx.redirect("/admin/usuarios/" + id + "/editar?error=Nombre,+username+y+rol+son+requeridos");
            return;
        }

        if (password != null && !password.isBlank() && password.length() < 4) {
            ctx.redirect("/admin/usuarios/" + id + "/editar?error=La+contraseña+debe+tener+al+menos+4+caracteres");
            return;
        }

        try {
            RolUsuario rol = RolUsuario.valueOf(rolStr);
            usuarioService.editarUsuario(id, nombre.trim(), username.trim(), password, rol);
            ctx.redirect("/admin/usuarios");
        } catch (Exception e) {
            ctx.redirect("/admin/usuarios/" + id + "/editar?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public static void eliminarUsuario(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        try {
            usuarioService.eliminarUsuario(id);
            ctx.json(Map.of("success", true));
        } catch (Exception e) {
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
