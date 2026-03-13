package pucmm.application.services;

import org.hibernate.Session;
import pucmm.application.models.Evento;
import pucmm.application.utils.HibernateUtil;

import java.util.List;

public class EventoService extends GenericService<Evento> {

    public EventoService() {
        super(Evento.class);
    }

    public List<Evento> findDisponibles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Evento e WHERE e.disponible = true AND e.cancelado = false ORDER BY e.fechaInicio ASC",
                    Evento.class)
                    .list();
        }
    }

    public List<Evento> findByOrganizador(Long organizadorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Evento e WHERE e.organizador.id = :orgId ORDER BY e.fechaCreacion DESC",
                    Evento.class)
                    .setParameter("orgId", organizadorId)
                    .list();
        }
    }

    public Evento publicar(Long eventoId) {
        Evento evento = find(eventoId);
        if (evento == null)
            throw new IllegalArgumentException("Evento no encontrado.");
        evento.setDisponible(true);
        return update(evento);
    }

    public Evento despublicar(Long eventoId) {
        Evento evento = find(eventoId);
        if (evento == null)
            throw new IllegalArgumentException("Evento no encontrado.");
        evento.setDisponible(false);
        return update(evento);
    }

    public Evento cancelar(Long eventoId) {
        Evento evento = find(eventoId);
        if (evento == null)
            throw new IllegalArgumentException("Evento no encontrado.");
        evento.setCancelado(true);
        evento.setDisponible(false);
        return update(evento);
    }
}
