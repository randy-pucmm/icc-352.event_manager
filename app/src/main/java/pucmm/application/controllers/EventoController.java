package pucmm.application.controllers;

import io.javalin.http.Context;
import pucmm.application.models.Evento;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.InscripcionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventoController {

    private static final EventoService eventoService = new EventoService();
    private static final InscripcionService inscripcionService = new InscripcionService();
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static void listar(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Evento> eventos = eventoService.findDisponibles();
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("eventos", eventos);
        model.put("pageTitle", "Eventos");
        model.put("currentPage", "eventos");
        ctx.render("eventos/lista.html", model);
    }

    public static void detalle(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.findWithInscripciones(id);
        if (evento == null) {
            ctx.redirect("/eventos");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("evento", evento);
        model.put("pageTitle", evento.getTitulo());
        model.put("currentPage", "eventos");
        // Check if current user is already inscribed
        boolean yaInscrito = inscripcionService.findByUsuarioAndEvento(usuario.getId(), id) != null;
        model.put("yaInscrito", yaInscrito);
        ctx.render("eventos/detalle.html", model);
    }

    public static void crearPage(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("pageTitle", "Crear Evento");
        model.put("currentPage", "crear-evento");
        model.put("editando", false);
        String error = ctx.queryParam("error");
        if (error != null) model.put("error", error);
        ctx.render("eventos/formulario.html", model);
    }

    public static void crear(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        String titulo = ctx.formParam("titulo");
        String descripcion = ctx.formParam("descripcion");
        String fechaInicioStr = ctx.formParam("fechaInicio");
        String fechaFinStr = ctx.formParam("fechaFin");
        String lugar = ctx.formParam("lugar");
        String cupoStr = ctx.formParam("cupoMaximo");

        if (titulo == null || titulo.isBlank() || fechaInicioStr == null || fechaFinStr == null
                || lugar == null || lugar.isBlank() || cupoStr == null) {
            ctx.redirect("/eventos/crear?error=Todos+los+campos+son+requeridos");
            return;
        }

        try {
            LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr);
            LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr);
            int cupoMaximo = Integer.parseInt(cupoStr);

            if (cupoMaximo < 1) {
                ctx.redirect("/eventos/crear?error=El+cupo+debe+ser+al+menos+1");
                return;
            }

            if (fechaFin.isBefore(fechaInicio)) {
                ctx.redirect("/eventos/crear?error=La+fecha+de+fin+debe+ser+posterior+a+la+de+inicio");
                return;
            }

            Evento evento = new Evento(titulo.trim(), descripcion != null ? descripcion.trim() : "",
                    fechaInicio, fechaFin, lugar.trim(), cupoMaximo, usuario);
            eventoService.save(evento);
            ctx.redirect("/mis-eventos");
        } catch (DateTimeParseException | NumberFormatException e) {
            ctx.redirect("/eventos/crear?error=Datos+invalidos");
        }
    }

    public static void editarPage(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.find(id);

        if (evento == null) {
            ctx.redirect("/mis-eventos");
            return;
        }

        // Only owner or admin can edit
        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.redirect("/eventos");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("evento", evento);
        model.put("pageTitle", "Editar Evento");
        model.put("currentPage", "mis-eventos");
        model.put("editando", true);
        model.put("fechaInicioStr", evento.getFechaInicio().format(DT_FORMAT));
        model.put("fechaFinStr", evento.getFechaFin().format(DT_FORMAT));
        String error = ctx.queryParam("error");
        if (error != null) model.put("error", error);
        ctx.render("eventos/formulario.html", model);
    }

    public static void editar(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.find(id);

        if (evento == null) {
            ctx.redirect("/mis-eventos");
            return;
        }

        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.redirect("/eventos");
            return;
        }

        String titulo = ctx.formParam("titulo");
        String descripcion = ctx.formParam("descripcion");
        String fechaInicioStr = ctx.formParam("fechaInicio");
        String fechaFinStr = ctx.formParam("fechaFin");
        String lugar = ctx.formParam("lugar");
        String cupoStr = ctx.formParam("cupoMaximo");

        if (titulo == null || titulo.isBlank() || fechaInicioStr == null || fechaFinStr == null
                || lugar == null || lugar.isBlank() || cupoStr == null) {
            ctx.redirect("/eventos/" + id + "/editar?error=Todos+los+campos+son+requeridos");
            return;
        }

        try {
            LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr);
            LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr);
            int cupoMaximo = Integer.parseInt(cupoStr);

            if (cupoMaximo < 1) {
                ctx.redirect("/eventos/" + id + "/editar?error=El+cupo+debe+ser+al+menos+1");
                return;
            }

            if (fechaFin.isBefore(fechaInicio)) {
                ctx.redirect("/eventos/" + id + "/editar?error=La+fecha+de+fin+debe+ser+posterior+a+la+de+inicio");
                return;
            }

            evento.setTitulo(titulo.trim());
            evento.setDescripcion(descripcion != null ? descripcion.trim() : "");
            evento.setFechaInicio(fechaInicio);
            evento.setFechaFin(fechaFin);
            evento.setLugar(lugar.trim());
            evento.setCupoMaximo(cupoMaximo);
            eventoService.update(evento);
            ctx.redirect("/mis-eventos");
        } catch (DateTimeParseException | NumberFormatException e) {
            ctx.redirect("/eventos/" + id + "/editar?error=Datos+invalidos");
        }
    }

    public static void togglePublicar(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.find(id);

        if (evento == null) {
            ctx.json(Map.of("success", false, "message", "Evento no encontrado"));
            return;
        }

        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.json(Map.of("success", false, "message", "No autorizado"));
            return;
        }

        try {
            if (evento.isDisponible()) {
                eventoService.despublicar(id);
            } else {
                eventoService.publicar(id);
            }
            ctx.json(Map.of("success", true, "disponible", !evento.isDisponible()));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void cancelar(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Long id = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoService.find(id);

        if (evento == null) {
            ctx.json(Map.of("success", false, "message", "Evento no encontrado"));
            return;
        }

        if (!evento.getOrganizador().getId().equals(usuario.getId())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            ctx.json(Map.of("success", false, "message", "No autorizado"));
            return;
        }

        try {
            eventoService.cancelar(id);
            ctx.json(Map.of("success", true));
        } catch (Exception e) {
            ctx.json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public static void misEventos(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        List<Evento> eventos = eventoService.findByOrganizador(usuario.getId());
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("eventos", eventos);
        model.put("pageTitle", "Mis Eventos");
        model.put("currentPage", "mis-eventos");
        ctx.render("eventos/mis-eventos.html", model);
    }
}
