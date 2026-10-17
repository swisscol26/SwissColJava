package seguridad;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public final class GestorSesiones {

    private static final SecureRandom generador = new SecureRandom();

    private static final ConcurrentHashMap<String, Sesion> sesiones =
        new ConcurrentHashMap<>();

    private GestorSesiones() {
    }

    public static String crearSesion(int usuarioId) {
        sesiones.entrySet().removeIf(
            entrada -> entrada.getValue().estaVencida()
        );

        Sesion sesion = new Sesion(
            usuarioId,
            Instant.now().plus(1, ChronoUnit.HOURS)
        );

        String token;

        do {
            byte[] bytes = new byte[32];
            generador.nextBytes(bytes);

            token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        } while (sesiones.putIfAbsent(token, sesion) != null);

        return token;
    }

    public static Sesion obtenerSesion(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        Sesion sesion = sesiones.get(token);

        if (sesion == null) {
            return null;
        }

        if (sesion.estaVencida()) {
            sesiones.remove(token, sesion);
            return null;
        }

        return sesion;
    }

    public static void cerrarSesion(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }
}
