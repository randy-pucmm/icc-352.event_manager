package pucmm.application.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.services.UsuarioService;

public class DatabaseSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    public static void seed(Dotenv dotenv) {
        UsuarioService usuarioService = new UsuarioService();

        String adminUsername = dotenv.get("ADMIN_USERNAME", "admin");
        String adminPassword = dotenv.get("ADMIN_PASSWORD", "admin123");

        Usuario admin = usuarioService.findByUsername(adminUsername);

        if (admin == null) {
            admin = new Usuario(
                    "Administrador",
                    adminUsername,
                    PasswordUtil.hashPassword(adminPassword),
                    RolUsuario.ADMINISTRADOR
            );
            admin.setEliminable(false);
            usuarioService.save(admin);
            logger.info("Admin user created: {}", adminUsername);
        } else {
            logger.info("Admin user already exists: {}", adminUsername);
        }
    }
}
