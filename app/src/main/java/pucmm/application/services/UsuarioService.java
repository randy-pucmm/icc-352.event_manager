package pucmm.application.services;

import org.hibernate.Session;
import pucmm.application.models.Usuario;
import pucmm.application.models.enums.RolUsuario;
import pucmm.application.utils.HibernateUtil;
import pucmm.application.utils.PasswordUtil;

public class UsuarioService extends GenericService<Usuario> {

    public UsuarioService() {
        super(Usuario.class);
    }

    public Usuario findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Usuario u WHERE u.username = :username", Usuario.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public Usuario authenticate(String username, String password) {
        Usuario usuario = findByUsername(username);
        if (usuario == null) return null;
        if (!usuario.isActivo()) return null;
        if (!PasswordUtil.checkPassword(password, usuario.getPasswordHash())) return null;
        return usuario;
    }

    public Usuario cambiarRol(Long userId, RolUsuario nuevoRol) {
        Usuario usuario = find(userId);
        if (usuario == null) throw new IllegalArgumentException("Usuario no encontrado.");
        usuario.setRol(nuevoRol);
        return update(usuario);
    }

    public Usuario bloquearUsuario(Long userId) {
        Usuario usuario = find(userId);
        if (usuario == null) throw new IllegalArgumentException("Usuario no encontrado.");
        if (!usuario.isEliminable()) throw new IllegalStateException("No se puede bloquear al administrador principal.");
        usuario.setActivo(!usuario.isActivo());
        return update(usuario);
    }

    public boolean existeUsername(String username) {
        return findByUsername(username) != null;
    }

    public Usuario registrar(String nombre, String username, String password, RolUsuario rol) {
        if (existeUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        Usuario usuario = new Usuario(nombre, username, PasswordUtil.hashPassword(password), rol);
        return save(usuario);
    }
}
