package controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import dto.CategoriaResultado;
import services.CategoriaService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CategoriaController {

    private static final Gson gson = new Gson();

    private static final CategoriaService categoriaService =
        new CategoriaService();

    private static final Set<String> ORIGENES_PERMITIDOS = config.ServidorConfig.origenes();

    private CategoriaController() {
    }

    public static void listar(
            HttpExchange exchange) throws IOException {

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

            responder(
                exchange,
                405,
                Map.of(
                    "mensaje",
                    "Método no permitido."
                )
            );
            return;
        }

        List<CategoriaResultado> categorias;

        try {
            categorias = categoriaService.listar();

        } catch (Exception e) {
            System.err.println(
                "Error al consultar categorías: "
                    + e.getClass().getSimpleName()
            );

            responder(
                exchange,
                500,
                Map.of(
                    "mensaje",
                    "No fue posible consultar las categorías."
                )
            );
            return;
        }

        responder(exchange, 200, categorias);
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

        exchange.sendResponseHeaders(
            codigo,
            contenido.length
        );

        try (OutputStream salida =
                exchange.getResponseBody()) {

            salida.write(contenido);
        }
    }
}