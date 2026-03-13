package pucmm.application.services;

import org.hibernate.Session;
import pucmm.application.models.Evento;
import pucmm.application.models.Inscripcion;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.EstadoInscripcion;
import pucmm.application.utils.HibernateUtil;

import java.util.List;

public class InscripcionService extends GenericService<Inscripcion> {

    public InscripcionService() {
        super(Inscripcion.class);
    }

    public Inscripcion findByUsuarioAndEvento(Long usuarioId, Long eventoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Inscripcion i WHERE i.usuario.id = :uid AND i.evento.id = :eid AND i.estado = :estado",
                    Inscripcion.class)
                    .setParameter("uid", usuarioId)
                    .setParameter("eid", eventoId)
                    .setParameter("estado", EstadoInscripcion.ACTIVA)
                    .uniqueResult();
        }
    }

    public List<Inscripcion> findByEvento(Long eventoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Inscripcion i WHERE i.evento.id = :eid AND i.estado = :estado",
                    Inscripcion.class)
                    .setParameter("eid", eventoId)
                    .setParameter("estado", EstadoInscripcion.ACTIVA)
                    .list();
        }
    }

    public List<Inscripcion> findByUsuario(Long usuarioId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Inscripcion i WHERE i.usuario.id = :uid ORDER BY i.fechaInscripcion DESC",
                    Inscripcion.class)
                    .setParameter("uid", usuarioId)
                    .list();
        }
    }

    public Inscripcion findByToken(String tokenQr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Inscripcion i WHERE i.tokenQr = :token",
                    Inscripcion.class)
                    .setParameter("token", tokenQr)
                    .uniqueResult();
        }
    }

    public Inscripcion inscribir(Usuario usuario, Evento evento) {
        // Validate event is available
        if (!evento.isDisponible() || evento.isCancelado()) {
            throw new IllegalStateException("El evento no está disponible para inscripción.");
        }

        // Validate no duplicate
        Inscripcion existente = findByUsuarioAndEvento(usuario.getId(), evento.getId());
        if (existente != null) {
            throw new IllegalStateException("Ya estás inscrito en este evento.");
        }

        // Validate capacity
        if (!evento.tieneCupo()) {
            throw new IllegalStateException("No hay cupo disponible en este evento.");
        }

        Inscripcion inscripcion = new Inscripcion(usuario, evento);
        return save(inscripcion);
    }

    public Inscripcion cancelarInscripcion(Long inscripcionId) {
        Inscripcion inscripcion = find(inscripcionId);
        if (inscripcion == null)
            throw new IllegalArgumentException("Inscripción no encontrada.");
        inscripcion.cancelarInscripcion();
        return update(inscripcion);
    }

    public Inscripcion marcarAsistencia(String tokenQr) {
        Inscripcion inscripcion = findByToken(tokenQr);
        if (inscripcion == null)
            throw new IllegalArgumentException("QR inválido.");
        inscripcion.marcarPresente();
        return update(inscripcion);
    }
}
