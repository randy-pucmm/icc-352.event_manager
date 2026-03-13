package pucmm.application.models;

import jakarta.persistence.*;
import pucmm.application.models.enums.EstadoInscripcion;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inscripciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "usuario_id", "evento_id" })
})
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado = EstadoInscripcion.ACTIVA;

    @Column(nullable = false, unique = true)
    private String tokenQr;

    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    @Column(nullable = false)
    private boolean estaPresente = false;

    private LocalDateTime fechaAsistencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    public Inscripcion() {
    }

    public Inscripcion(Usuario usuario, Evento evento) {
        this.usuario = usuario;
        this.evento = evento;
        this.estado = EstadoInscripcion.ACTIVA;
        this.tokenQr = UUID.randomUUID().toString();
        this.fechaInscripcion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDateTime.now();
        }
        if (tokenQr == null) {
            tokenQr = UUID.randomUUID().toString();
        }
    }

    // Business methods

    public void marcarPresente() {
        if (this.estaPresente) {
            throw new IllegalStateException("La asistencia ya fue registrada.");
        }
        if (this.estado != EstadoInscripcion.ACTIVA) {
            throw new IllegalStateException("La inscripción no está activa.");
        }
        this.estaPresente = true;
        this.fechaAsistencia = LocalDateTime.now();
    }

    public void cancelarInscripcion() {
        if (this.estado == EstadoInscripcion.CANCELADA) {
            throw new IllegalStateException("La inscripción ya está cancelada.");
        }
        this.estado = EstadoInscripcion.CANCELADA;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public String getTokenQr() {
        return tokenQr;
    }

    public void setTokenQr(String tokenQr) {
        this.tokenQr = tokenQr;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public boolean isEstaPresente() {
        return estaPresente;
    }

    public void setEstaPresente(boolean estaPresente) {
        this.estaPresente = estaPresente;
    }

    public LocalDateTime getFechaAsistencia() {
        return fechaAsistencia;
    }

    public void setFechaAsistencia(LocalDateTime fechaAsistencia) {
        this.fechaAsistencia = fechaAsistencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
