package pucmm.application.filters;

import io.javalin.http.Context;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.UsuarioService;

public class AuthFilter {

    private static final UsuarioService usuarioService = new UsuarioService();

    public static void requireAuth(Context ctx) {
        Long usuarioId = ctx.sessionAttribute("usuarioId");
        if (usuarioId == null) {
            ctx.redirect("/login");
            ctx.skipRemainingHandlers();
            return;
        }
        Usuario usuario = usuarioService.find(usuarioId);
        if (usuario == null || !usuario.isActivo()) {
            ctx.req().getSession().invalidate();
            ctx.redirect("/login");
            ctx.skipRemainingHandlers();
            return;
        }
        ctx.attribute("usuario", usuario);
    }

    public static void requireAdmin(Context ctx) {
        requireAuth(ctx);
        Usuario usuario = ctx.attribute("usuario");
        if (usuario != null && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.redirect("/dashboard");
            ctx.skipRemainingHandlers();
        }
    }

    public static void requireOrganizador(Context ctx) {
        requireAuth(ctx);
        Usuario usuario = ctx.attribute("usuario");
        if (usuario != null
                && usuario.getRol() != RolUsuario.ORGANIZADOR
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.redirect("/dashboard");
            ctx.skipRemainingHandlers();
        }
    }
}
