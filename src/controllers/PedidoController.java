package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;

import dto.PedidoResultado;
import dto.PedidoSolicitud;
import dto.PedidoHistorialResultado;
import dto.ConfirmarPedidoSolicitud;
import dto.PedidoAdminResultado;
import dto.EstadisticasVentasResultado;
import seguridad.AccesoException;
import services.PedidoService;

import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public final class PedidoController {

    private static final Gson gson = new Gson();

    private static final PedidoService pedidoService =
        new PedidoService();

    private static final Set<String> ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private PedidoController() {
    }

    public static void crear(
            HttpExchange exchange) throws IOException {

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
            return;
        }

        if (origen != null) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
        }

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Credentials",
            "true"
        );

        exchange.getResponseHeaders().set("Vary", "Origin");

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            "POST, OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.getResponseHeaders().set(
                "Allow",
                "POST, OPTIONS"
            );

            responderError(
                exchange,
                405,
                "Método no permitido."
            );
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
                "Debes enviar el pedido en formato JSON."
            );
            return;
        }

        byte[] contenido = exchange
            .getRequestBody()
            .readNBytes(65537);

        if (contenido.length > 65536) {
            responderError(
                exchange,
                413,
                "La solicitud es demasiado grande."
            );
            return;
        }

        PedidoResultado resultado;

        try {
            String json = new String(
                contenido,
                StandardCharsets.UTF_8
            );

            PedidoSolicitud solicitud = gson.fromJson(
                json,
                PedidoSolicitud.class
            );

            resultado = pedidoService.crear(
                obtenerTokenSesion(exchange),
                solicitud
            );

        } catch (SecurityException e) {
            responderError(exchange, 401, e.getMessage());
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
                "Error al guardar el pedido: "
                    + e.getClass().getSimpleName()
            );

            responderError(
                exchange,
                500,
                "No fue posible guardar el pedido."
            );
            return;
        }

        responder(exchange, 201, resultado);
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

                String[] partes =
                    cookie.trim().split("=", 2);

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

        exchange.getResponseHeaders().set(
            "Cache-Control",
            "no-store"
        );

        exchange.sendResponseHeaders(
            codigo,
            contenido.length
        );

        try (OutputStream salida =
                exchange.getResponseBody()) {

            salida.write(contenido);
        }
    }

    public static void listar(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials",
        "true"
    );

    exchange.getResponseHeaders().set("Vary", "Origin");

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods",
        "GET, OPTIONS"
    );

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"GET".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow",
            "GET, OPTIONS"
        );

        responderError(
            exchange,
            405,
            "Método no permitido."
        );
        return;
    }

    List<PedidoHistorialResultado> pedidos;

    try {
        pedidos = pedidoService.listar(
            obtenerTokenSesion(exchange)
        );

    } catch (SecurityException e) {
        responderError(exchange, 401, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al consultar pedidos: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible consultar tus pedidos."
        );
        return;
    }

    responder(exchange, 200, pedidos);
}

public static void confirmar(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials",
        "true"
    );

    exchange.getResponseHeaders().set("Vary", "Origin");

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods",
        "POST, OPTIONS"
    );

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers",
        "Content-Type"
    );

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow",
            "POST, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
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

    PedidoResultado resultado;

    try {
        ConfirmarPedidoSolicitud solicitud = gson.fromJson(
            new String(contenido, StandardCharsets.UTF_8),
            ConfirmarPedidoSolicitud.class
        );

        if (solicitud == null || solicitud.getPedidoId() <= 0) {
            throw new IllegalArgumentException(
                "Debes indicar un número de pedido válido."
            );
        }

        resultado = pedidoService.confirmar(
            obtenerTokenSesion(exchange),
            solicitud.getPedidoId()
        );

    } catch (AccesoException e) {
        responderError(exchange, e.getCodigoHttp(), e.getMessage());
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

    } catch (IllegalStateException e) {
        responderError(exchange, 409, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al confirmar el pedido: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible confirmar el pedido."
        );
        return;
    }

    responder(exchange, 200, resultado);
}

public static void listarPendientes(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials",
        "true"
    );

    exchange.getResponseHeaders().set("Vary", "Origin");

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods",
        "GET, OPTIONS"
    );

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"GET".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow",
            "GET, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
        return;
    }

    List<PedidoAdminResultado> pedidos;

    try {
        pedidos = pedidoService.listarPendientes(
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
            "Error al consultar pedidos pendientes: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible consultar los pedidos pendientes."
        );
        return;
    }

    responder(exchange, 200, pedidos);
}

public static void consultarEstadisticas(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials",
        "true"
    );

    exchange.getResponseHeaders().set("Vary", "Origin");

    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods",
        "GET, OPTIONS"
    );

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"GET".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow",
            "GET, OPTIONS"
        );

        responderError(
            exchange,
            405,
            "Método no permitido."
        );
        return;
    }

    EstadisticasVentasResultado resultado;

    try {
        resultado = pedidoService.consultarEstadisticas(
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
            "Error al consultar estadísticas: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible consultar las estadísticas."
        );
        return;
    }

    responder(exchange, 200, resultado);
}

public static void actualizarCostoEnvio(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials", "true"
    );
    exchange.getResponseHeaders().set("Vary", "Origin");
    exchange.getResponseHeaders().set(
        "Cache-Control", "no-store"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods", "POST, OPTIONS"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers", "Content-Type"
    );

    if (!"/api/pedidos/costo-envio".equals(
            exchange.getRequestURI().getPath())) {

        responderError(exchange, 404, "Ruta no encontrada.");
        return;
    }

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow", "POST, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
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
        dto.CostoEnvioSolicitud solicitud = gson.fromJson(
            new String(contenido, StandardCharsets.UTF_8),
            dto.CostoEnvioSolicitud.class
        );

        pedidoService.actualizarCostoEnvio(
            obtenerTokenSesion(exchange),
            solicitud
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

    } catch (IllegalStateException e) {
        responderError(exchange, 409, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al actualizar el costo de envío: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible actualizar el costo de envío."
        );
        return;
    }

    responder(
        exchange,
        200,
        Map.of(
            "exito", true,
            "mensaje", "Costo de envío actualizado."
        )
    );
}

public static void aprobarParaPago(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials", "true"
    );
    exchange.getResponseHeaders().set("Vary", "Origin");
    exchange.getResponseHeaders().set(
        "Cache-Control", "no-store"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods", "POST, OPTIONS"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers", "Content-Type"
    );

    if (!"/api/pedidos/aprobar".equals(
            exchange.getRequestURI().getPath())) {

        responderError(exchange, 404, "Ruta no encontrada.");
        return;
    }

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow", "POST, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
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
        ConfirmarPedidoSolicitud solicitud = gson.fromJson(
            new String(contenido, StandardCharsets.UTF_8),
            ConfirmarPedidoSolicitud.class
        );

        if (solicitud == null || solicitud.getPedidoId() <= 0) {
            throw new IllegalArgumentException(
                "Debes indicar un número de pedido válido."
            );
        }

        pedidoService.aprobarParaPago(
            obtenerTokenSesion(exchange),
            solicitud.getPedidoId()
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

    } catch (IllegalStateException e) {
        responderError(exchange, 409, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al aprobar el pedido: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible aprobar el pedido."
        );
        return;
    }

    responder(
        exchange,
        200,
        Map.of(
            "exito", true,
            "mensaje",
            "Pedido aprobado. Unidades reservadas durante 24 horas."
        )
    );
}

public static void cancelarSolicitud(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials", "true"
    );
    exchange.getResponseHeaders().set("Vary", "Origin");
    exchange.getResponseHeaders().set(
        "Cache-Control", "no-store"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods", "POST, OPTIONS"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers", "Content-Type"
    );

    if (!"/api/pedidos/cancelar".equals(
            exchange.getRequestURI().getPath())) {

        responderError(exchange, 404, "Ruta no encontrada.");
        return;
    }

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow", "POST, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
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
        ConfirmarPedidoSolicitud solicitud = gson.fromJson(
            new String(contenido, StandardCharsets.UTF_8),
            ConfirmarPedidoSolicitud.class
        );

        if (solicitud == null || solicitud.getPedidoId() <= 0) {
            throw new IllegalArgumentException(
                "Debes indicar un número de pedido válido."
            );
        }

        pedidoService.cancelarSolicitud(
            obtenerTokenSesion(exchange),
            solicitud.getPedidoId()
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

    } catch (IllegalStateException e) {
        responderError(exchange, 409, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al cancelar la solicitud: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible cancelar la solicitud."
        );
        return;
    }

    responder(
        exchange,
        200,
        Map.of(
            "exito", true,
            "mensaje",
            "Solicitud cancelada. Se liberaron sus reservas de inventario."
        )
    );
}

public static void reportarPago(
        HttpExchange exchange) throws IOException {

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
        return;
    }

    if (origen != null) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
    }
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Credentials", "true"
    );
    exchange.getResponseHeaders().set("Vary", "Origin");
    exchange.getResponseHeaders().set(
        "Cache-Control", "no-store"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Methods", "POST, OPTIONS"
    );
    exchange.getResponseHeaders().set(
        "Access-Control-Allow-Headers", "Content-Type"
    );

    if (!"/api/pedidos/reportar-pago".equals(
            exchange.getRequestURI().getPath())) {

        responderError(exchange, 404, "Ruta no encontrada.");
        return;
    }

    if ("OPTIONS".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (!"POST".equalsIgnoreCase(
            exchange.getRequestMethod())) {

        exchange.getResponseHeaders().set(
            "Allow", "POST, OPTIONS"
        );

        responderError(exchange, 405, "Método no permitido.");
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
        dto.PagoReportadoSolicitud solicitud =
            gson.fromJson(
                new String(contenido, StandardCharsets.UTF_8),
                dto.PagoReportadoSolicitud.class
            );

        pedidoService.reportarPago(
            obtenerTokenSesion(exchange),
            solicitud
        );

    } catch (SecurityException e) {
        responderError(exchange, 401, e.getMessage());
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

    } catch (IllegalStateException e) {
        responderError(exchange, 409, e.getMessage());
        return;

    } catch (Exception e) {
        System.err.println(
            "Error al reportar el pago: "
                + e.getClass().getSimpleName()
        );

        responderError(
            exchange,
            500,
            "No fue posible registrar el reporte de pago."
        );
        return;
    }

    responder(
        exchange,
        200,
        Map.of(
            "exito", true,
            "mensaje",
            "Pago reportado. Espera la verificación del vendedor."
        )
    );
}
}