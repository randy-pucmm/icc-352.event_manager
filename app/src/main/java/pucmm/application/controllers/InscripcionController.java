package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Evento;
import pucmm.application.models.Inscripcion;
import pucmm.application.models.Usuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.InscripcionService;

import pucmm.application.models.enums.RolUsuario;

import java.time.format.DateTimeFormatter;
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

    public static void escanearQrPage(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Evento> eventos = eventoService.findByOrganizador(usuario.getId());
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("eventos", eventos);
        model.put("pageTitle", "Escanear QR");
        model.put("currentPage", "escanear-qr");
        ctx.render("inscripciones/escanear-qr.html", model);
    }

    public static void verificarQr(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        String token = ctx.formParam("token");

        if (token == null || token.isBlank()) {
            ctx.json(Map.of("success", false, "message", "Token QR requerido"));
            return;
        }

        Inscripcion inscripcion = inscripcionService.findByTokenWithDetails(token.trim());
        if (inscripcion == null) {
            ctx.json(Map.of("success", false, "message", "QR no válido. No se encontró ninguna inscripción."));
            return;
        }

        // Verify the organizer owns this event (or is admin)
        Evento evento = inscripcion.getEvento();
        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.json(Map.of("success", false, "message", "No tienes permiso para escanear QR de este evento."));
            return;
        }

        if (inscripcion.isEstaPresente()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            ctx.json(Map.of(
                    "success", false,
                    "message", "La asistencia ya fue registrada el " + inscripcion.getFechaAsistencia().format(fmt),
                    "participante", inscripcion.getUsuario().getNombre(),
                    "evento", evento.getTitulo(),
                    "yaRegistrado", true
            ));
            return;
        }

        if (inscripcion.getEstado() != pucmm.application.models.enums.EstadoInscripcion.ACTIVA) {
            ctx.json(Map.of("success", false, "message", "La inscripción no está activa (estado: " + inscripcion.getEstado() + ")."));
            return;
        }

        try {
            inscripcionService.marcarAsistencia(token.trim());
            ctx.json(Map.of(
                    "success", true,
                    "message", "Asistencia registrada correctamente",
                    "participante", inscripcion.getUsuario().getNombre(),
                    "evento", evento.getTitulo()
            ));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void listaAsistencia(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long eventoId = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.findWithInscripciones(eventoId);

        if (evento == null) {
            ctx.redirect("/mis-eventos");
            return;
        }

        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.redirect("/mis-eventos");
            return;
        }

        List<Inscripcion> inscripciones = inscripcionService.findByEventoWithUsuario(eventoId);
        long totalInscritos = inscripciones.size();
        long totalPresentes = inscripciones.stream().filter(Inscripcion::isEstaPresente).count();

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("evento", evento);
        model.put("inscripciones", inscripciones);
        model.put("totalInscritos", totalInscritos);
        model.put("totalPresentes", totalPresentes);
        model.put("pageTitle", "Asistencia - " + evento.getTitulo());
        model.put("currentPage", "mis-eventos");
        ctx.render("inscripciones/asistencia.html", model);
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
