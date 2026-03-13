package pucmm.application;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.rendering.FileRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pucmm.application.controllers.AuthController;
import pucmm.application.controllers.DashboardController;
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

                // Organizer routes
                before("/mis-eventos", AuthFilter::requireOrganizador);
                before("/mis-eventos/*", AuthFilter::requireOrganizador);
                before("/eventos/crear", AuthFilter::requireOrganizador);
                before("/eventos/crear/*", AuthFilter::requireOrganizador);
                before("/escanear-qr", AuthFilter::requireOrganizador);
                before("/escanear-qr/*", AuthFilter::requireOrganizador);

                // General authenticated routes
                before("/eventos", AuthFilter::requireAuth);
                before("/eventos/*", AuthFilter::requireAuth);
                before("/mis-inscripciones", AuthFilter::requireAuth);
                before("/mis-inscripciones/*", AuthFilter::requireAuth);
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
