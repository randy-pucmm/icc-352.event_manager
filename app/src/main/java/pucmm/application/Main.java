package pucmm.application;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.rendering.FileRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pucmm.application.controllers.AdminController;
import pucmm.application.controllers.AuthController;
import pucmm.application.controllers.DashboardController;
import pucmm.application.controllers.EventoController;
import pucmm.application.controllers.InscripcionController;
import pucmm.application.filters.AuthFilter;
import pucmm.application.utils.DatabaseSeeder;
import pucmm.application.utils.HibernateUtil;
import pucmm.application.utils.ThymeleafConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Initialize Hibernate and create tables
        logger.info("Initializing database...");
        HibernateUtil.init(dotenv);
        logger.info("Database initialized successfully.");

        // Seed initial data
        DatabaseSeeder.seed(dotenv);

        // Configure Thymeleaf
        FileRenderer thymeleaf = new ThymeleafConfig();

        // Start Javalin server
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(thymeleaf);

            config.routes.apiBuilder(() -> {
                // Public routes
                get("/", ctx -> ctx.redirect("/dashboard"));
                get("/login", AuthController::loginPage);
                post("/login", AuthController::login);
                get("/registro", AuthController::registroPage);
                post("/registro", AuthController::registro);
                get("/logout", AuthController::logout);

                // Protected routes - require authentication
                before("/dashboard", AuthFilter::requireAuth);
                before("/dashboard/*", AuthFilter::requireAuth);
                get("/dashboard", DashboardController::dashboard);

                // Admin routes
                before("/admin/*", AuthFilter::requireAdmin);
                get("/admin/usuarios", AdminController::listarUsuarios);
                post("/admin/usuarios/{id}/bloquear", AdminController::toggleBloquear);
                post("/admin/usuarios/{id}/rol", AdminController::cambiarRol);
                get("/admin/usuarios/crear", AdminController::crearUsuarioPage);
                post("/admin/usuarios/crear", AdminController::crearUsuario);
                get("/admin/usuarios/{id}/editar", AdminController::editarUsuarioPage);
                post("/admin/usuarios/{id}/editar", AdminController::editarUsuario);
                post("/admin/usuarios/{id}/eliminar", AdminController::eliminarUsuario);
                get("/admin/eventos", AdminController::listarEventos);
                post("/admin/eventos/{id}/eliminar", AdminController::eliminarEvento);

                // Organizer routes
                before("/mis-eventos", AuthFilter::requireOrganizador);
                before("/mis-eventos/*", AuthFilter::requireOrganizador);
                before("/eventos/crear", AuthFilter::requireOrganizador);
                before("/eventos/crear/*", AuthFilter::requireOrganizador);
                before("/escanear-qr", AuthFilter::requireOrganizador);
                before("/escanear-qr/*", AuthFilter::requireOrganizador);
                get("/escanear-qr", InscripcionController::escanearQrPage);
                post("/escanear-qr/verificar", InscripcionController::verificarQr);
                get("/mis-eventos", EventoController::misEventos);
                get("/eventos/crear", EventoController::crearPage);
                post("/eventos/crear", EventoController::crear);

                // General authenticated routes
                before("/eventos", AuthFilter::requireAuth);
                before("/eventos/*", AuthFilter::requireAuth);
                get("/eventos", EventoController::listar);
                get("/eventos/{id}", EventoController::detalle);
                get("/eventos/{id}/editar", EventoController::editarPage);
                post("/eventos/{id}/editar", EventoController::editar);
                post("/eventos/{id}/publicar", EventoController::togglePublicar);
                post("/eventos/{id}/cancelar", EventoController::cancelar);
                post("/eventos/{id}/inscribir", InscripcionController::inscribirse);
                get("/eventos/{id}/asistencia", InscripcionController::listaAsistencia);
                get("/eventos/{id}/estadisticas", DashboardController::estadisticasEvento);

                // Inscription routes
                before("/mis-inscripciones", AuthFilter::requireAuth);
                before("/mis-inscripciones/*", AuthFilter::requireAuth);
                get("/mis-inscripciones", InscripcionController::misInscripciones);
                before("/inscripciones/*", AuthFilter::requireAuth);
                post("/inscripciones/{id}/cancelar", InscripcionController::cancelar);
            });
        });

        app.start(7000);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            app.stop();
            HibernateUtil.shutdown();
        }));

        logger.info("Application started on http://localhost:7000");
    }
}
