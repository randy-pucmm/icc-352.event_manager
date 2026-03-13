package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.InscripcionService;
import pucmm.application.services.UsuarioService;

import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    private static final UsuarioService usuarioService = new UsuarioService();
    private static final EventoService eventoService = new EventoService();
    private static final InscripcionService inscripcionService = new InscripcionService();

    public static void dashboard(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("pageTitle", "Dashboard");
        model.put("currentPage", "dashboard");

        // Stats for dashboard
        int totalEventos = eventoService.findDisponibles().size();
        model.put("totalEventos", totalEventos);

        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            model.put("totalUsuarios", usuarioService.findAll().size());
            model.put("totalEventosSistema", eventoService.findAll().size());
        }

        if (usuario.getRol() == RolUsuario.ORGANIZADOR || usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            model.put("misEventos", eventoService.findByOrganizador(usuario.getId()).size());
        }

        if (usuario.getRol() == RolUsuario.PARTICIPANTE) {
            model.put("misInscripciones", inscripcionService.findByUsuario(usuario.getId()).size());
        }

        ctx.render("dashboard.html", model);
    }
}
