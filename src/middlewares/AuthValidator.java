package middlewares;

import dto.LoginSolicitud;
import dto.RegistroSolicitud;

import java.util.regex.Pattern;

public final class AuthValidator {

    private static final Pattern PATRON_CORREO =
        Pattern.compile(
            "^[A-Za-z0-9._%+-]+"
                + "@[A-Za-z0-9.-]+"
                + "\\.[A-Za-z]{2,}$"
        );

    private AuthValidator() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Valida los datos enviados para registrar un usuario.
     */
    public static String validarRegistro(
            RegistroSolicitud solicitud) {

        if (solicitud == null) {
            return "El cuerpo de la solicitud es obligatorio.";
        }

        String nombre = solicitud.getNombre();
        String correo = solicitud.getCorreo();
        String password = solicitud.getPassword();
        String confirmacion =
            solicitud.getConfirmarPassword();

        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio.";
        }

        if (nombre.trim().length() < 3
                || nombre.trim().length() > 50) {
            return "El nombre debe tener entre 3 y 50 caracteres.";
        }

        String errorCorreo = validarCorreo(correo);

        if (errorCorreo != null) {
            return errorCorreo;
        }

        if (password == null || password.isBlank()) {
            return "La contraseña es obligatoria.";
        }

        if (password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe incluir una mayúscula.";
        }

        if (!password.matches(".*[a-z].*")) {
            return "La contraseña debe incluir una minúscula.";
        }

        if (!password.matches(".*[0-9].*")) {
            return "La contraseña debe incluir un número.";
        }

        if (confirmacion == null
                || !password.equals(confirmacion)) {
            return "Las contraseñas no coinciden.";
        }

        return null;
    }

    /**
     * Valida los datos enviados para iniciar sesión.
     */
    public static String validarLogin(
            LoginSolicitud solicitud) {

        if (solicitud == null) {
            return "El cuerpo de la solicitud es obligatorio.";
        }

        String errorCorreo =
            validarCorreo(solicitud.getCorreo());

        if (errorCorreo != null) {
            return errorCorreo;
        }

        if (solicitud.getPassword() == null
                || solicitud.getPassword().isBlank()) {
            return "La contraseña es obligatoria.";
        }

        return null;
    }

    private static String validarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return "El correo es obligatorio.";
        }

        String correoLimpio = correo.trim();

        if (correoLimpio.length() > 100) {
            return "El correo admite máximo 100 caracteres.";
        }

        if (!PATRON_CORREO
                .matcher(correoLimpio)
                .matches()) {
            return "El formato del correo no es válido.";
        }

        return null;
    }
}