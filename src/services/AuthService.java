package services;

import dao.UsuarioDAO;
import dto.AuthResultado;
import dto.LoginSolicitud;
import dto.RegistroSolicitud;
import middlewares.AuthValidator;
import modelo.Usuario;
import seguridad.PasswordUtil;

import java.sql.SQLException;
import java.util.Locale;

public class AuthService {

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Valida y registra un usuario nuevo.
     */
    public AuthResultado registrar(
            RegistroSolicitud solicitud) {

        String error =
            AuthValidator.validarRegistro(solicitud);

        if (error != null) {
            return AuthResultado.error(400, error);
        }

        String correo =
            normalizarCorreo(solicitud.getCorreo());

        try {

            Usuario existente =
                usuarioDAO.buscarPorCorreo(correo);

            if (existente != null) {
                return AuthResultado.error(
                    409,
                    "El correo ya está registrado."
                );
            }

            Usuario usuario = new Usuario();

            usuario.setUserName(
                normalizarNombre(solicitud.getNombre())
            );

            usuario.setEmail(correo);

            usuario.setPasswordHash(
                PasswordUtil.crearHash(
                    solicitud.getPassword()
                )
            );

            // Los registros públicos siempre crean clientes.
            usuario.setUserRole("CUSTOMER");

            int usuarioId =
                usuarioDAO.insertarUsuario(usuario);

            return AuthResultado.exitoso(
                201,
                "Usuario registrado correctamente.",
                usuarioId,
                usuario.getUserName(),
                usuario.getEmail(),
                usuario.getUserRole()
            );

        } catch (SQLException e) {

            System.err.println(
                "Error al registrar usuario: "
                    + e.getMessage()
            );

            if ("23000".equals(e.getSQLState())) {
                return AuthResultado.error(
                    409,
                    "El correo ya está registrado."
                );
            }

            return AuthResultado.error(
                500,
                "Error interno del servidor."
            );

        } catch (IllegalStateException e) {

            System.err.println(e.getMessage());

            return AuthResultado.error(
                500,
                "No fue posible proteger la contraseña."
            );
        }
    }

    /**
     * Comprueba el correo y la contraseña del usuario.
     */
    public AuthResultado iniciarSesion(
            LoginSolicitud solicitud) {

        String error =
            AuthValidator.validarLogin(solicitud);

        if (error != null) {
            return AuthResultado.error(400, error);
        }

        String correo =
            normalizarCorreo(solicitud.getCorreo());

        try {

            Usuario usuario =
                usuarioDAO.buscarPorCorreo(correo);

            if (usuario == null
                    || !PasswordUtil.verificar(
                        solicitud.getPassword(),
                        usuario.getPasswordHash()
                    )) {

                return AuthResultado.error(
                    401,
                    "Correo o contraseña incorrectos."
                );
            }

            return AuthResultado.exitoso(
                200,
                "Inicio de sesión exitoso.",
                usuario.getUserId(),
                usuario.getUserName(),
                usuario.getEmail(),
                usuario.getUserRole()
            );

        } catch (SQLException e) {

            System.err.println(
                "Error al iniciar sesión: "
                    + e.getMessage()
            );

            return AuthResultado.error(
                500,
                "Error interno del servidor."
            );
        }
    }

    private String normalizarCorreo(String correo) {

        return correo
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private String normalizarNombre(String nombre) {

        return nombre
            .trim()
            .replaceAll("\\s+", " ");
    }
}