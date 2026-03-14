package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Evento;
import pucmm.application.models.Inscripcion;
import pucmm.application.models.Usuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.InscripcionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InscripcionController {

    private static final InscripcionService inscripcionService = new InscripcionService();
    private static final EventoService eventoService = new EventoService();

    public static void inscribirse(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long eventoId = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.findWithInscripciones(eventoId);

        if (evento == null) {
            ctx.json(Map.of("success", false, "message", "Evento no encontrado"));
            return;
        }

        try {
            inscripcionService.inscribir(usuario, evento);
            ctx.json(Map.of("success", true, "message", "Inscripción exitosa"));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void misInscripciones(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Inscripcion> inscripciones = inscripcionService.findByUsuarioWithEvento(usuario.getId());
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("inscripciones", inscripciones);
        model.put("pageTitle", "Mis Inscripciones");
        model.put("currentPage", "inscripciones");
        ctx.render("inscripciones/mis-inscripciones.html", model);
    }

    public static void cancelar(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long inscripcionId = Long.parseLong(ctx.pathParam("id"));
        Inscripcion inscripcion = inscripcionService.find(inscripcionId);

        if (inscripcion == null) {
            ctx.json(Map.of("success", false, "message", "Inscripción no encontrada"));
            return;
        }

        // Only the owner can cancel
        if (!inscripcion.getUsuario().getId().equals(usuario.getId())) {
            ctx.json(Map.of("success", false, "message", "No autorizado"));
            return;
        }

        try {
            inscripcionService.cancelarInscripcion(inscripcionId);
            ctx.json(Map.of("success", true));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
