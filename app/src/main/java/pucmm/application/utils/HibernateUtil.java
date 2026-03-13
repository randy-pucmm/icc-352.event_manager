package pucmm.application.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import pucmm.application.models.Evento;
import pucmm.application.models.Inscripcion;
import pucmm.application.models.Usuario;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static void init(Dotenv dotenv) {
        if (sessionFactory != null) return;

        String dbUrl = dotenv.get("DB_URL", "jdbc:h2:./data/eventos;AUTO_SERVER=TRUE");
        String dbUser = dotenv.get("DB_USER", "sa");
        String dbPassword = dotenv.get("DB_PASSWORD", "");

        Configuration configuration = new Configuration();

        // H2 Database settings
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.username", dbUser);
        configuration.setProperty("hibernate.connection.password", dbPassword);

        // Hibernate settings
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "true");

        // Connection pool
        configuration.setProperty("hibernate.hikari.minimumIdle", "2");
        configuration.setProperty("hibernate.hikari.maximumPoolSize", "10");

        // Register entities
        configuration.addAnnotatedClass(Usuario.class);
        configuration.addAnnotatedClass(Evento.class);
        configuration.addAnnotatedClass(Inscripcion.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("HibernateUtil not initialized. Call init() first.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
