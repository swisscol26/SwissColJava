package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import dto.ProductoResultado;
import modelo.Producto;
import services.ProductoService;
import services.AutorizacionService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public final class ProductoController {

    private static final String RUTA_BASE =
        "/api/productos";

    private static final Gson gson =
        new GsonBuilder().serializeNulls().create();

    private static final ProductoService productoService =
        new ProductoService();

        private static final AutorizacionService autorizacionService =
    new AutorizacionService();

    private static final Set<String>
        ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private ProductoController() {
        // Evita crear objetos de esta clase.
    }

    public static void manejar(
            HttpExchange exchange) throws IOException {

        configurarCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {

            Integer productoId = obtenerIdDesdeRuta(
                exchange.getRequestURI().getPath()
            );

            String metodo = exchange
                .getRequestMethod()
                .toUpperCase();

                if ("POST".equals(metodo)
        || "PUT".equals(metodo)
        || "DELETE".equals(metodo)) {

    String origen = exchange
        .getRequestHeaders()
        .getFirst("Origin");

    if (origen == null
            || !ORIGENES_PERMITIDOS.contains(origen)) {

        enviarResultado(
            exchange,
            ProductoResultado.error(
                403,
                "Origen de la solicitud no permitido."
            )
        );
        return;
    }

    int codigo = autorizacionService.comprobarAdministrador(
        obtenerTokenSesion(exchange)
    );

    if (codigo != 200) {

        String mensaje = codigo == 401
            ? "Debes iniciar sesión."
            : "Solo el administrador puede modificar productos.";

        enviarResultado(
            exchange,
            ProductoResultado.error(codigo, mensaje)
        );
        return;
    }
}

            switch (metodo) {
                case "GET" -> manejarGet(exchange, productoId);
                case "POST" -> manejarPost(exchange, productoId);
                case "PUT" -> manejarPut(exchange, productoId);
                case "DELETE" -> manejarDelete(exchange, productoId);
                default -> {
                    exchange.getResponseHeaders().set(
                        "Allow",
                        "GET, POST, PUT, DELETE, OPTIONS"
                    );

                    enviarResultado(
                        exchange,
                        ProductoResultado.error(
                            405,
                            "Método no permitido."
                        )
                    );
                }
            }

        } catch (RutaProductoException e) {

            enviarResultado(
                exchange,
                ProductoResultado.error(
                    e.getCodigoHttp(),
                    e.getMessage()
                )
            );

        } catch (JsonSyntaxException e) {

            enviarResultado(
                exchange,
                ProductoResultado.error(
                    400,
                    "El contenido JSON no es válido."
                )
            );

        } catch (Exception e) {

            System.err.println(
                "Error en la API de productos: "
                    + e.getMessage()
            );

            enviarResultado(
                exchange,
                ProductoResultado.error(
                    500,
                    "Error interno del servidor."
                )
            );
        }
    }

    private static void manejarGet(
            HttpExchange exchange,
            Integer productoId) throws IOException {

        if (productoId == null) {

            List<Producto> productos =
                productoService.listarProductos();

            enviarRespuesta(
                exchange,
                200,
                gson.toJson(productos)
            );

            return;
        }

        ProductoResultado resultado =
            productoService.buscarProducto(productoId);

        if (resultado.isExito()) {
            enviarRespuesta(
                exchange,
                resultado.getCodigoHttp(),
                gson.toJson(resultado.getProducto())
            );
        } else {
            enviarResultado(exchange, resultado);
        }
    }

    private static void manejarPost(
            HttpExchange exchange,
            Integer productoId) throws IOException {

        if (productoId != null) {
            enviarResultado(
                exchange,
                ProductoResultado.error(
                    400,
                    "No incluya un ID en la ruta para registrar."
                )
            );
            return;
        }

        if (!validarContenidoJson(exchange)) {
            return;
        }

        Producto producto = leerProducto(exchange);

        enviarResultado(
            exchange,
            productoService.registrarProducto(producto)
        );
    }

    private static void manejarPut(
            HttpExchange exchange,
            Integer productoId) throws IOException {

        if (productoId == null) {
            enviarResultado(
                exchange,
                ProductoResultado.error(
                    400,
                    "Debe indicar el ID del producto."
                )
            );
            return;
        }

        if (!validarContenidoJson(exchange)) {
            return;
        }

        Producto producto = leerProducto(exchange);

        enviarResultado(
            exchange,
            productoService.actualizarProducto(
                productoId,
                producto
            )
        );
    }

    private static void manejarDelete(
            HttpExchange exchange,
            Integer productoId) throws IOException {

        if (productoId == null) {
            enviarResultado(
                exchange,
                ProductoResultado.error(
                    400,
                    "Debe indicar el ID del producto."
                )
            );
            return;
        }

        enviarResultado(
            exchange,
            productoService.eliminarProducto(productoId)
        );
    }

    private static Producto leerProducto(
            HttpExchange exchange) throws IOException {

        byte[] contenido =
            exchange.getRequestBody().readAllBytes();

        String json = new String(
            contenido,
            StandardCharsets.UTF_8
        ).trim();

        if (json.isBlank()) {
            throw new JsonSyntaxException("Cuerpo vacío.");
        }

        Producto producto = gson.fromJson(
            json,
            Producto.class
        );

        if (producto == null) {
            throw new JsonSyntaxException("Producto vacío.");
        }

        return producto;
    }

    private static boolean validarContenidoJson(
            HttpExchange exchange) throws IOException {

        String tipoContenido = exchange
            .getRequestHeaders()
            .getFirst("Content-Type");

        if (tipoContenido != null
                && tipoContenido
                    .toLowerCase()
                    .startsWith("application/json")) {
            return true;
        }

        enviarResultado(
            exchange,
            ProductoResultado.error(
                415,
                "El Content-Type debe ser application/json."
            )
        );

        return false;
    }

    private static Integer obtenerIdDesdeRuta(
            String ruta) {

        if (RUTA_BASE.equals(ruta)
                || (RUTA_BASE + "/").equals(ruta)) {
            return null;
        }

        if (!ruta.startsWith(RUTA_BASE + "/")) {
            throw new RutaProductoException(
                404,
                "Ruta de productos no encontrada."
            );
        }

        String idTexto = ruta.substring(
            (RUTA_BASE + "/").length()
        );

        if (idTexto.isBlank() || idTexto.contains("/")) {
            throw new RutaProductoException(
                404,
                "Ruta de productos no encontrada."
            );
        }

        try {

            int productoId = Integer.parseInt(idTexto);

            if (productoId <= 0) {
                throw new RutaProductoException(
                    400,
                    "El ID debe ser un número entero mayor que cero."
                );
            }

            return productoId;

        } catch (NumberFormatException e) {
            throw new RutaProductoException(
                400,
                "El ID debe ser un número entero válido."
            );
        }
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
            "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    }

    private static void enviarResultado(
            HttpExchange exchange,
            ProductoResultado resultado) throws IOException {

        enviarRespuesta(
            exchange,
            resultado.getCodigoHttp(),
            gson.toJson(resultado)
        );
    }

    private static void enviarRespuesta(
            HttpExchange exchange,
            int codigo,
            String respuesta) throws IOException {

        byte[] contenido = respuesta.getBytes(
            StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
            codigo,
            contenido.length
        );

        try (
            OutputStream salida =
                exchange.getResponseBody()
        ) {
            salida.write(contenido);
        }
    }

    private static final class RutaProductoException
            extends RuntimeException {

        private final int codigoHttp;

        private RutaProductoException(
                int codigoHttp,
                String mensaje) {

            super(mensaje);
            this.codigoHttp = codigoHttp;
        }

        private int getCodigoHttp() {
            return codigoHttp;
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
}
