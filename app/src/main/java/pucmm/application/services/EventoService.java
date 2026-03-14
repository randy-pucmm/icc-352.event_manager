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
                    "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.inscripciones WHERE e.disponible = true AND e.cancelado = false ORDER BY e.fechaInicio ASC",
                    Evento.class)
                    .list();
        }
    }

    public List<Evento> findByOrganizador(Long organizadorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.inscripciones WHERE e.organizador.id = :orgId ORDER BY e.fechaCreacion DESC",
                    Evento.class)
                    .setParameter("orgId", organizadorId)
                    .list();
        }
    }

    public Evento findWithInscripciones(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Evento evento = session.createQuery(
                    "SELECT e FROM Evento e LEFT JOIN FETCH e.inscripciones LEFT JOIN FETCH e.organizador WHERE e.id = :id",
                    Evento.class)
                    .setParameter("id", id)
                    .uniqueResult();
            return evento;
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

    public List<Evento> findAllOrdered() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.inscripciones LEFT JOIN FETCH e.organizador ORDER BY e.fechaCreacion DESC",
                    Evento.class)
                    .list();
        }
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
