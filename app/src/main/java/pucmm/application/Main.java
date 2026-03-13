package pucmm.application;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pucmm.application.utils.DatabaseSeeder;
import pucmm.application.utils.HibernateUtil;

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

        // Start Javalin server
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.routes.apiBuilder(() -> {
                io.javalin.apibuilder.ApiBuilder.get("/", ctx -> ctx.result("Event Manager API - Running"));
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
