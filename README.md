# Sistema de Gestión y Control de Eventos Académicos

Proyecto del segundo parcial de la asignatura **ICC-352 Programación Web** de la Pontificia Universidad Católica Madre y Maestra (PUCMM).

## Autores

- Randy Alexander Germosén Ureña
- Hugo Fernando Concepción López

## Profesor

- Carlos Alfredo Camacho Guerrero

## Descripción

Aplicación web para la gestión de eventos académicos que permite la creación de eventos, inscripción de participantes con códigos QR, control de asistencia mediante escaneo QR y visualización de estadísticas por rol de usuario.

## Tecnologías

- **Backend**: Java 21, Javalin 7, Hibernate ORM 6, H2 Database
- **Frontend**: Thymeleaf, Bootstrap 5, Chart.js, html5-qrcode
- **Build**: Gradle 9.2.1, ShadowJar
- **Despliegue**: Docker

## Roles del Sistema

| Rol | Funcionalidades |
|-----|----------------|
| **Administrador** | Gestión de usuarios, gestión global de eventos, dashboard con estadísticas generales |
| **Organizador** | Crear/editar eventos, escanear QR para asistencia, ver estadísticas de sus eventos |
| **Participante** | Explorar eventos, inscribirse, ver su código QR, consultar sus inscripciones |

## Despliegue con Docker

```bash
docker compose up -d --build
```

El contenedor expone el puerto `30015` mapeado al `7000` interno. Los datos de H2 se persisten en un volumen Docker nombrado.

## Documentación

El reporte detallado del proyecto se encuentra en el directorio `report/` en formato PDF.
