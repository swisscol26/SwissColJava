package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import dto.AuthResultado;
import dto.LoginSolicitud;
import dto.RegistroSolicitud;
import services.AuthService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class AuthController {

    private static final Gson gson = new Gson();

    private static final AuthService authService =
        new AuthService();

    private static final Set<String>
        ORIGENES_PERMITIDOS = Set.of(
            "http://localhost:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500"
        );

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

        try {

            LoginSolicitud solicitud =
                leerJson(
                    exchange,
                    LoginSolicitud.class
                );

            AuthResultado resultado =
                authService.iniciarSesion(solicitud);

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
                "Error en login: " + e.getMessage()
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
}