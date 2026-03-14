package pucmm.application.controllers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import pucmm.application.models.Evento;
import pucmm.application.models.Inscripcion;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.EventoService;
import pucmm.application.services.InscripcionService;
import pucmm.application.services.UsuarioService;

import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    private static final UsuarioService usuarioService = new UsuarioService();
    private static final EventoService eventoService = new EventoService();
    private static final InscripcionService inscripcionService = new InscripcionService();
    private static final Gson gson = new Gson();

    public static void dashboard(Context ctx) {
        Usuario usuario = ctx.attribute("usuario");
        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("pageTitle", "Dashboard");
        model.put("currentPage", "dashboard");

        // Basic stats
        List<Evento> eventosDisponibles = eventoService.findDisponibles();
        model.put("totalEventos", eventosDisponibles.size());

        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            List<Usuario> allUsers = usuarioService.findAll();
            List<Evento> allEventos = eventoService.findAllOrdered();
            model.put("totalUsuarios", allUsers.size());
            model.put("totalEventosSistema", allEventos.size());

            // Admin charts: users by role
            Map<String, Long> usersByRole = allUsers.stream()
                    .collect(Collectors.groupingBy(u -> u.getRol().name(), Collectors.counting()));
            model.put("usersByRoleLabels", gson.toJson(new ArrayList<>(usersByRole.keySet())));
            model.put("usersByRoleData", gson.toJson(new ArrayList<>(usersByRole.values())));

            // Admin charts: events status breakdown
            long publicados = allEventos.stream().filter(e -> !e.isCancelado() && e.isDisponible()).count();
            long borradores = allEventos.stream().filter(e -> !e.isCancelado() && !e.isDisponible()).count();
            long cancelados = allEventos.stream().filter(Evento::isCancelado).count();
            model.put("eventStatusLabels", gson.toJson(Arrays.asList("Publicados", "Borradores", "Cancelados")));
            model.put("eventStatusData", gson.toJson(Arrays.asList(publicados, borradores, cancelados)));

            // Admin charts: top events by inscriptions
            List<Evento> topEventos = allEventos.stream()
                    .filter(e -> !e.isCancelado())
                    .sorted((a, b) -> Integer.compare(b.obtenerInscritos().size(), a.obtenerInscritos().size()))
                    .limit(5)
                    .collect(Collectors.toList());
            List<String> topNames = topEventos.stream().map(e -> e.getTitulo().length() > 20 ? e.getTitulo().substring(0, 20) + "..." : e.getTitulo()).collect(Collectors.toList());
            List<Integer> topInscritos = topEventos.stream().map(e -> e.obtenerInscritos().size()).collect(Collectors.toList());
            List<Integer> topAsistentes = topEventos.stream().map(e -> e.obtenerAsistencias().size()).collect(Collectors.toList());
            model.put("topEventLabels", gson.toJson(topNames));
            model.put("topEventInscritos", gson.toJson(topInscritos));
            model.put("topEventAsistentes", gson.toJson(topAsistentes));

            // Total attendance stats
            long totalInscritos = allEventos.stream().mapToLong(e -> e.obtenerInscritos().size()).sum();
            long totalAsistentes = allEventos.stream().mapToLong(e -> e.obtenerAsistencias().size()).sum();
            model.put("totalInscritos", totalInscritos);
            model.put("totalAsistentes", totalAsistentes);
        }

        if (usuario.getRol() == RolUsuario.ORGANIZADOR || usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            List<Evento> misEventos = eventoService.findByOrganizador(usuario.getId());
            model.put("misEventos", misEventos.size());

            // Organizer charts: per-event attendance
            List<Evento> activeEventos = misEventos.stream().filter(e -> !e.isCancelado()).limit(8).collect(Collectors.toList());
            List<String> evNames = activeEventos.stream().map(e -> e.getTitulo().length() > 18 ? e.getTitulo().substring(0, 18) + "..." : e.getTitulo()).collect(Collectors.toList());
            List<Integer> evInscritos = activeEventos.stream().map(e -> e.obtenerInscritos().size()).collect(Collectors.toList());
            List<Integer> evAsistentes = activeEventos.stream().map(e -> e.obtenerAsistencias().size()).collect(Collectors.toList());
            model.put("orgEventLabels", gson.toJson(evNames));
            model.put("orgEventInscritos", gson.toJson(evInscritos));
            model.put("orgEventAsistentes", gson.toJson(evAsistentes));

            // Organizer total stats
            long orgTotalInscritos = misEventos.stream().mapToLong(e -> e.obtenerInscritos().size()).sum();
            long orgTotalAsistentes = misEventos.stream().mapToLong(e -> e.obtenerAsistencias().size()).sum();
            model.put("orgTotalInscritos", orgTotalInscritos);
            model.put("orgTotalAsistentes", orgTotalAsistentes);
        }

        if (usuario.getRol() == RolUsuario.PARTICIPANTE) {
            List<Inscripcion> inscripciones = inscripcionService.findByUsuarioWithEvento(usuario.getId());
            model.put("misInscripciones", inscripciones.size());

            long activas = inscripciones.stream().filter(i -> i.getEstado().name().equals("ACTIVA") && !i.isEstaPresente()).count();
            long asistidas = inscripciones.stream().filter(i -> i.getEstado().name().equals("ACTIVA") && i.isEstaPresente()).count();
            long canceladas = inscripciones.stream().filter(i -> i.getEstado().name().equals("CANCELADA")).count();
            model.put("partStatusLabels", gson.toJson(Arrays.asList("Activas", "Asistidas", "Canceladas")));
            model.put("partStatusData", gson.toJson(Arrays.asList(activas, asistidas, canceladas)));
        }

        ctx.render("dashboard.html", model);
    }

    public static void estadisticasEvento(Context ctx) {
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

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuario);
        model.put("evento", evento);
        model.put("pageTitle", "Estadísticas - " + evento.getTitulo());
        model.put("currentPage", "mis-eventos");

        int totalInscritos = evento.obtenerInscritos().size();
        int totalAsistentes = evento.obtenerAsistencias().size();
        double porcentaje = evento.calcularPorcentajeAsistencia();

        model.put("totalInscritos", totalInscritos);
        model.put("totalAsistentes", totalAsistentes);
        model.put("porcentajeAsistencia", String.format("%.1f", porcentaje));
        model.put("cuposDisponibles", evento.getCuposDisponibles());

        // Attendance donut
        model.put("asistenciaLabels", gson.toJson(Arrays.asList("Presentes", "Pendientes")));
        model.put("asistenciaData", gson.toJson(Arrays.asList(totalAsistentes, totalInscritos - totalAsistentes)));

        // Inscriptions per day
        Map<String, Long> porDia = new TreeMap<>(evento.calcularInscripcionesPorDia());
        model.put("porDiaLabels", gson.toJson(new ArrayList<>(porDia.keySet())));
        model.put("porDiaData", gson.toJson(new ArrayList<>(porDia.values())));

        // Attendance by hour
        Map<Integer, Long> porHora = new TreeMap<>(evento.calcularAsistenciaPorHora());
        List<String> horaLabels = new ArrayList<>();
        List<Long> horaData = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            if (porHora.containsKey(h)) {
                horaLabels.add(String.format("%02d:00", h));
                horaData.add(porHora.get(h));
            }
        }
        if (horaLabels.isEmpty()) {
            horaLabels.add("Sin datos");
            horaData.add(0L);
        }
        model.put("porHoraLabels", gson.toJson(horaLabels));
        model.put("porHoraData", gson.toJson(horaData));

        // Capacity donut
        model.put("capacidadLabels", gson.toJson(Arrays.asList("Inscritos", "Disponibles")));
        model.put("capacidadData", gson.toJson(Arrays.asList(totalInscritos, evento.getCuposDisponibles())));

        ctx.render("eventos/estadisticas.html", model);
    }
}
