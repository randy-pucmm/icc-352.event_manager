package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.UsuarioService;

import java.util.HashMap;
import java.util.Map;

public class AuthController {

    private static final UsuarioService usuarioService = new UsuarioService();

    public static void loginPage(Context ctx) {
        if (ctx.sessionAttribute("usuarioId") != null) {
            ctx.redirect("/dashboard");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        String error = ctx.queryParam("error");
        String success = ctx.queryParam("success");
        if (error != null) model.put("error", error);
        if (success != null) model.put("success", success);
        ctx.render("login.html", model);
    }

    public static void login(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            ctx.redirect("/login?error=Todos+los+campos+son+requeridos");
            return;
        }

        Usuario usuario = usuarioService.authenticate(username.trim(), password);
        if (usuario == null) {
            ctx.redirect("/login?error=Credenciales+inválidas+o+cuenta+bloqueada");
            return;
        }

        ctx.sessionAttribute("usuarioId", usuario.getId());
        ctx.redirect("/dashboard");
    }

    public static void registroPage(Context ctx) {
        if (ctx.sessionAttribute("usuarioId") != null) {
            ctx.redirect("/dashboard");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        String error = ctx.queryParam("error");
        if (error != null) model.put("error", error);
        ctx.render("registro.html", model);
    }

    public static void registro(Context ctx) {
        String nombre = ctx.formParam("nombre");
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirmPassword");

        if (nombre == null || nombre.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            ctx.redirect("/registro?error=Todos+los+campos+son+requeridos");
            return;
        }

        if (!password.equals(confirmPassword)) {
            ctx.redirect("/registro?error=Las+contraseñas+no+coinciden");
            return;
        }

        if (password.length() < 6) {
            ctx.redirect("/registro?error=La+contraseña+debe+tener+al+menos+6+caracteres");
            return;
        }

        try {
            Usuario usuario = usuarioService.registrar(
                    nombre.trim(), username.trim(), password, RolUsuario.PARTICIPANTE);
            ctx.sessionAttribute("usuarioId", usuario.getId());
            ctx.redirect("/dashboard");
        } catch (IllegalArgumentException e) {
            ctx.redirect("/registro?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/login?success=Sesión+cerrada+correctamente");
    }
}
