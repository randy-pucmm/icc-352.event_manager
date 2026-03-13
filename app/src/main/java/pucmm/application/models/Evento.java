package pucmm.application.models;

import jakarta.persistence.*;
import pucmm.application.models.enums.EstadoInscripcion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private String lugar;

    @Column(nullable = false)
    private int cupoMaximo;

    @Column(nullable = false)
    private boolean disponible = false;

    @Column(nullable = false)
    private boolean cancelado = false;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    public Evento() {
    }

    public Evento(String titulo, String descripcion, LocalDateTime fechaInicio,
            LocalDateTime fechaFin, String lugar, int cupoMaximo, Usuario organizador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.lugar = lugar;
        this.cupoMaximo = cupoMaximo;
        this.organizador = organizador;
        this.fechaCreacion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    // Business methods

    public List<Inscripcion> obtenerInscritos() {
        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .collect(Collectors.toList());
    }

    public List<Inscripcion> obtenerAsistencias() {
        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA && i.isEstaPresente())
                .collect(Collectors.toList());
    }

    public double calcularPorcentajeAsistencia() {
        List<Inscripcion> inscritos = obtenerInscritos();
        if (inscritos.isEmpty())
            return 0.0;
        long asistentes = inscritos.stream().filter(Inscripcion::isEstaPresente).count();
        return (asistentes * 100.0) / inscritos.size();
    }

    public Map<String, Long> calcularInscripcionesPorDia() {
        return obtenerInscritos().stream()
                .collect(Collectors.groupingBy(
                        i -> i.getFechaInscripcion().toLocalDate().toString(),
                        Collectors.counting()));
    }

    public Map<Integer, Long> calcularAsistenciaPorHora() {
        return obtenerAsistencias().stream()
                .filter(i -> i.getFechaAsistencia() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getFechaAsistencia().getHour(),
                        Collectors.counting()));
    }

    public int getCuposDisponibles() {
        return cupoMaximo - obtenerInscritos().size();
    }

    public boolean tieneCupo() {
        return getCuposDisponibles() > 0;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }
}
