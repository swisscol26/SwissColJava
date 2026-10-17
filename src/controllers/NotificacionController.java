package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;

import dto.NotificacionResultado;
import seguridad.AccesoException;
import services.NotificacionService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NotificacionController {

    private static final Gson gson = new Gson();

    private static final NotificacionService servicio =
        new NotificacionService();

    private static final Set<String> ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private NotificacionController() {
    }

    public static void listar(
            HttpExchange exchange) throws IOException {

        if (!prepararSolicitud(
                exchange,
                "/api/notificaciones",
                "GET")) {
            return;
        }

        List<NotificacionResultado> resultado;

        try {
            resultado = servicio.listar(
                obtenerTokenSesion(exchange)
            );

        } catch (AccesoException e) {
            responderError(
                exchange,
                e.getCodigoHttp(),
                e.getMessage()
            );
            return;

        } catch (Exception e) {
            System.err.println(
                "Error al consultar notificaciones: "
                    + e.getClass().getSimpleName()
            );

            responderError(
                exchange,
                500,
                "No fue posible consultar las notificaciones."
            );
            return;
        }

        responder(exchange, 200, resultado);
    }

    public static void marcarComoLeida(
            HttpExchange exchange) throws IOException {

        if (!prepararSolicitud(
                exchange,
                "/api/notificaciones/leida",
                "POST")) {
            return;
        }

        String tipoContenido = exchange
            .getRequestHeaders()
            .getFirst("Content-Type");

        if (tipoContenido == null
                || !tipoContenido.split(";", 2)[0]
                    .trim()
                    .equalsIgnoreCase("application/json")) {

            responderError(
                exchange,
                415,
                "Debes enviar la solicitud en formato JSON."
            );
            return;
        }

        byte[] contenido = exchange
            .getRequestBody()
            .readNBytes(4097);

        if (contenido.length > 4096) {
            responderError(
                exchange,
                413,
                "La solicitud es demasiado grande."
            );
            return;
        }

        try {
            MarcarLeidaSolicitud solicitud = gson.fromJson(
                new String(contenido, StandardCharsets.UTF_8),
                MarcarLeidaSolicitud.class
            );

            if (solicitud == null) {
                throw new IllegalArgumentException(
                    "Debes indicar la notificación."
                );
            }

            servicio.marcarComoLeida(
                obtenerTokenSesion(exchange),
                solicitud.notificacionId
            );

        } catch (AccesoException e) {
            responderError(
                exchange,
                e.getCodigoHttp(),
                e.getMessage()
            );
            return;

        } catch (JsonParseException e) {
            responderError(
                exchange,
                400,
                "El contenido JSON no es válido."
            );
            return;

        } catch (IllegalArgumentException e) {
            responderError(exchange, 400, e.getMessage());
            return;

        } catch (Exception e) {
            System.err.println(
                "Error al marcar la notificación: "
                    + e.getClass().getSimpleName()
            );

            responderError(
                exchange,
                500,
                "No fue posible actualizar la notificación."
            );
            return;
        }

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static boolean prepararSolicitud(
            HttpExchange exchange,
            String ruta,
            String metodo) throws IOException {

        exchange.getResponseHeaders().set(
            "Cache-Control",
            "no-store"
        );

        exchange.getResponseHeaders().set("Vary", "Origin");

        String origen = exchange
            .getRequestHeaders()
            .getFirst("Origin");

        if ((origen == null && !"GET".equals(exchange.getRequestMethod()))
                || (origen != null && !ORIGENES_PERMITIDOS.contains(origen))) {

            responderError(
                exchange,
                403,
                "Origen de la solicitud no permitido."
            );
            return false;
        }

        if (origen != null) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
        }

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Credentials",
            "true"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            metodo + ", OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );

        if (!ruta.equals(exchange.getRequestURI().getPath())) {
            responderError(exchange, 404, "Ruta no encontrada.");
            return false;
        }

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return false;
        }

        if (!metodo.equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.getResponseHeaders().set(
                "Allow",
                metodo + ", OPTIONS"
            );

            responderError(
                exchange,
                405,
                "Método no permitido."
            );
            return false;
        }

        return true;
    }

    private static String obtenerTokenSesion(
            HttpExchange exchange) {

        List<String> cabeceras = exchange
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

    private static void responderError(
            HttpExchange exchange,
            int codigo,
            String mensaje) throws IOException {

        responder(
            exchange,
            codigo,
            Map.of(
                "exito", false,
                "mensaje", mensaje
            )
        );
    }

    private static void responder(
            HttpExchange exchange,
            int codigo,
            Object respuesta) throws IOException {

        byte[] contenido = gson
            .toJson(respuesta)
            .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(codigo, contenido.length);

        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(contenido);
        }
    }

    private static class MarcarLeidaSolicitud {
        private int notificacionId;
    }
}