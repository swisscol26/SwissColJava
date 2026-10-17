package controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import config.PagoConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public final class PagoConfigController {

    private static final Gson gson = new Gson();

    private static final Set<String> ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private PagoConfigController() {
    }

    public static void obtener(HttpExchange exchange)
            throws IOException {

        prepararCors(exchange);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");

        if (!"/api/pagos/configuracion".equals(exchange.getRequestURI().getPath())) {
            responder(exchange, 404, Map.of("mensaje", "Ruta no encontrada."));
            return;
        }

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

            responder(
                exchange,
                405,
                Map.of("mensaje", "Método no permitido.")
            );
            return;
        }

        responder(
            exchange,
            200,
            Map.of(
                "metodo", "Nequi",
                "numero", PagoConfig.getNequiNumero(),
                "titular", PagoConfig.getNequiTitular(),
                "moneda", "COP",
                "plazoHoras", PagoConfig.PLAZO_HORAS
            )
        );
    }

    private static void prepararCors(HttpExchange exchange) {

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

        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            "GET, OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
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
}
