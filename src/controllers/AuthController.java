package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import dto.AuthResultado;
import dto.LoginSolicitud;
import dto.RegistroSolicitud;
import services.AuthService;
import services.AutorizacionService;
import seguridad.GestorSesiones;
import seguridad.Sesion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class AuthController {

    private static final Gson gson = new Gson();

    private static final AuthService authService =
        new AuthService();

        private static final AutorizacionService autorizacionService =
            new AutorizacionService();

    private static final Set<String>
        ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private AuthController() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Atiende POST /api/auth/registro.
     */
    public static void registrar(
            HttpExchange exchange) throws IOException {

        if (prepararSolicitud(exchange)) {
            return;
        }

        if (!validarMetodoPost(exchange)) {
            return;
        }

        try {

            RegistroSolicitud solicitud =
                leerJson(
                    exchange,
                    RegistroSolicitud.class
                );

            AuthResultado resultado =
                authService.registrar(solicitud);

            enviarResultado(exchange, resultado);

        } catch (JsonSyntaxException e) {

            enviarResultado(
                exchange,
                AuthResultado.error(
                    400,
                    "El contenido JSON no es válido."
                )
            );

        } catch (Exception e) {

            System.err.println(
                "Error en registro: " + e.getMessage()
            );

            enviarResultado(
                exchange,
                AuthResultado.error(
                    500,
                    "Error interno del servidor."
                )
            );
        }
    }

    /**
     * Atiende POST /api/auth/login.
     */
    public static void iniciarSesion(
            HttpExchange exchange) throws IOException {

        if (prepararSolicitud(exchange)) {
            return;
        }

        if (!validarMetodoPost(exchange)) {
            return;
        }

        AuthResultado resultado;

        try {
            LoginSolicitud solicitud = leerJson(
                exchange,
                LoginSolicitud.class
            );

            resultado = authService.iniciarSesion(solicitud);

            if (resultado.isExito()) {
                String token = GestorSesiones.crearSesion(
                    resultado.getUsuarioId()
                );

                exchange.getResponseHeaders().set(
                    "Set-Cookie",
                    "swisscol_sesion=" + token
                        + "; Path=/api"
                        + "; HttpOnly"
                        + "; SameSite=Lax"
                        + config.ServidorConfig.cookieSegura()
                        + "; Max-Age=3600"
                );
            }

        } catch (JsonSyntaxException e) {
            resultado = AuthResultado.error(
                400,
                "El contenido JSON no es válido."
            );

        } catch (Exception e) {
            System.err.println(
                "Error en login: " + e.getMessage()
            );

            resultado = AuthResultado.error(
                500,
                "Error interno del servidor."
            );
        }

        enviarResultado(exchange, resultado);
    }

    /**
     * Configura CORS y responde las solicitudes OPTIONS.
     */
    private static boolean prepararSolicitud(
            HttpExchange exchange) throws IOException {

        configurarCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            exchange.close();

            return true;
        }

        return false;
    }

    private static boolean validarMetodoPost(
            HttpExchange exchange) throws IOException {

        if ("POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {
            return true;
        }

        exchange.getResponseHeaders().set(
            "Allow",
            "POST, OPTIONS"
        );

        enviarResultado(
            exchange,
            AuthResultado.error(
                405,
                "Método no permitido."
            )
        );

        return false;
    }

    private static <T> T leerJson(
            HttpExchange exchange,
            Class<T> tipo) throws IOException {

        byte[] contenido =
            exchange.getRequestBody().readAllBytes();

        String json = new String(
            contenido,
            StandardCharsets.UTF_8
        ).trim();

        if (json.isBlank()) {
            throw new JsonSyntaxException(
                "Cuerpo vacío."
            );
        }

        T solicitud = gson.fromJson(json, tipo);

        if (solicitud == null) {
            throw new JsonSyntaxException(
                "Contenido vacío."
            );
        }

        return solicitud;
    }

    private static void configurarCors(
            HttpExchange exchange) {

        String origen = exchange
            .getRequestHeaders()
            .getFirst("Origin");

        if (origen != null
                && ORIGENES_PERMITIDOS.contains(origen)) {

            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                origen
            );
            exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials",
    "true"
);

            exchange.getResponseHeaders().set(
                "Vary",
                "Origin"
            );
        }

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            "POST, OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    }

    private static void enviarResultado(
            HttpExchange exchange,
            AuthResultado resultado) throws IOException {

        String json = gson.toJson(resultado);

        byte[] contenido =
            json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
            resultado.getCodigoHttp(),
            contenido.length
        );

        try (
            OutputStream salida =
                exchange.getResponseBody()
        ) {
            salida.write(contenido);
        }
    }

    private static String obtenerTokenSesion(
        HttpExchange exchange) {

    var cabeceras = exchange
        .getRequestHeaders()
        .get("Cookie");

    if (cabeceras == null) {
        return null;
    }

    for (String cabecera : cabeceras) {

        for (String cookie : cabecera.split(";")) {

            String[] partes = cookie.trim().split("=", 2);

            if (partes.length == 2
                    && "swisscol_sesion".equals(partes[0])) {

                return partes[1];
            }
        }
    }

    return null;
}

public static void cerrarSesion(
        HttpExchange exchange) throws IOException {

    if (prepararSolicitud(exchange)) {
        return;
    }

    if (!validarMetodoPost(exchange)) {
        return;
    }

    String origen = exchange
        .getRequestHeaders()
        .getFirst("Origin");

    if (origen == null
            || !ORIGENES_PERMITIDOS.contains(origen)) {

        enviarResultado(
            exchange,
            AuthResultado.error(
                403,
                "Origen de la solicitud no permitido."
            )
        );

        return;
    }

    String token = obtenerTokenSesion(exchange);

    GestorSesiones.cerrarSesion(token);

    exchange.getResponseHeaders().add(
        "Set-Cookie",
        "swisscol_sesion=; Path=/api"
            + "; HttpOnly"
            + "; SameSite=Lax"
                        + config.ServidorConfig.cookieSegura()
            + "; Max-Age=0"
    );

    exchange.sendResponseHeaders(204, -1);
    exchange.close();
}

public static void verificarSesion(
        HttpExchange exchange) throws IOException {

    if (prepararSolicitud(exchange)) {
        return;
    }

    if (!validarMetodoPost(exchange)) {
        return;
    }

    String token = obtenerTokenSesion(exchange);

    Sesion sesion = GestorSesiones.obtenerSesion(token);

    if (sesion == null) {

        enviarResultado(
            exchange,
            AuthResultado.error(
                401,
                "La sesión venció o no has iniciado sesión."
            )
        );

        return;
    }

    String json = gson.toJson(
        java.util.Map.of(
            "exito", true,
            "usuarioId", sesion.getUsuarioId()
        )
    );

    byte[] contenido = json.getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().set(
        "Content-Type",
        "application/json; charset=UTF-8"
    );

    exchange.getResponseHeaders().set(
        "Cache-Control",
        "no-store"
    );

    exchange.sendResponseHeaders(200, contenido.length);

    try (OutputStream salida = exchange.getResponseBody()) {
        salida.write(contenido);
    }
}

public static void verificarAdministrador(
        HttpExchange exchange) throws IOException {

    if (prepararSolicitud(exchange)) {
        return;
    }

    if (!validarMetodoPost(exchange)) {
        return;
    }

    String origen = exchange
        .getRequestHeaders()
        .getFirst("Origin");

    if (origen == null
            || !ORIGENES_PERMITIDOS.contains(origen)) {

        enviarResultado(
            exchange,
            AuthResultado.error(
                403,
                "Origen de la solicitud no permitido."
            )
        );
        return;
    }

    exchange.getResponseHeaders().set(
        "Cache-Control",
        "no-store"
    );

    int codigo;

    try {
        codigo = autorizacionService.comprobarAdministrador(
            obtenerTokenSesion(exchange)
        );

    } catch (Exception e) {
        System.err.println(
            "Error al comprobar acceso administrativo: "
                + e.getClass().getSimpleName()
        );

        enviarResultado(
            exchange,
            AuthResultado.error(
                500,
                "No fue posible comprobar los permisos."
            )
        );
        return;
    }

    if (codigo != 200) {

        String mensaje = codigo == 401
            ? "Debes iniciar sesión."
            : "Solo el administrador puede acceder al panel.";

        enviarResultado(
            exchange,
            AuthResultado.error(codigo, mensaje)
        );
        return;
    }

    exchange.sendResponseHeaders(204, -1);
    exchange.close();
}
}
