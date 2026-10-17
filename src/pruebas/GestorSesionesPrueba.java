package pruebas;

import seguridad.GestorSesiones;
import seguridad.Sesion;
import java.time.Instant;

public class GestorSesionesPrueba {

    public static void main(String[] args) {

        // Usuario ficticio: esta prueba no consulta la base de datos.
        String token = GestorSesiones.crearSesion(7);

        Sesion sesion = GestorSesiones.obtenerSesion(token);

        comprobar(
            sesion != null && sesion.getUsuarioId() == 7,
            "La sesión identifica al usuario correcto"
        );

        comprobar(
            GestorSesiones.obtenerSesion("token-inexistente") == null,
            "Rechaza un token desconocido"
        );

        comprobar(
            GestorSesiones.obtenerSesion(null) == null,
            "Rechaza un token vacío"
        );

        Sesion vencida = new Sesion(
            7,
            Instant.now().minusSeconds(1)
        );

        comprobar(
            vencida.estaVencida(),
            "Detecta una sesión vencida"
        );

        GestorSesiones.cerrarSesion(token);

        comprobar(
            GestorSesiones.obtenerSesion(token) == null,
            "La sesión cerrada ya no permite acceso"
        );

        System.out.println("Las 5 comprobaciones fueron aprobadas.");
    }

    private static void comprobar(
            boolean condicion,
            String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }

        System.out.println("OK: " + mensaje);
    }
}