package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import dao.ProductoDAO;
import modelo.Producto;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProductoApi {

    private static final int PUERTO = 8080;
    private static final Gson gson = new Gson();
    private static final ProductoDAO productoDAO = new ProductoDAO();

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(PUERTO),
            0
        );

        servidor.createContext(
            "/api/productos",
            ProductoApi::manejarPeticion
        );

        servidor.setExecutor(null);
        servidor.start();

        System.out.println(
            "API de productos ejecutándose en http://localhost:"
                + PUERTO + "/api/productos"
        );
    }

    /**
     * Atiende las solicitudes HTTP del módulo de productos.
     */
    private static void manejarPeticion(HttpExchange exchange)
            throws IOException {

        configurarCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {

            if (!"GET".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                enviarRespuesta(
                    exchange,
                    405,
                    "{\"mensaje\":\"Método no permitido\"}"
                );

                return;
            }

            String ruta = exchange.getRequestURI().getPath();
            String rutaBase = "/api/productos";

            if (ruta.equals(rutaBase)
                    || ruta.equals(rutaBase + "/")) {

                listarProductos(exchange);

            } else if (ruta.startsWith(rutaBase + "/")) {

                String idTexto = ruta.substring(
                    (rutaBase + "/").length()
                );

                int id = Integer.parseInt(idTexto);

                buscarProducto(exchange, id);

            } else {

                enviarRespuesta(
                    exchange,
                    404,
                    "{\"mensaje\":\"Ruta no encontrada\"}"
                );
            }

        } catch (NumberFormatException e) {

            enviarRespuesta(
                exchange,
                400,
                "{\"mensaje\":\"El ID debe ser un número entero\"}"
            );

        } catch (Exception e) {

            enviarRespuesta(
                exchange,
                500,
                "{\"mensaje\":\"Error interno del servidor\"}"
            );
        }
    }

    /**
     * Devuelve todos los productos registrados.
     */
    private static void listarProductos(HttpExchange exchange)
            throws IOException {

        List<Producto> productos =
            productoDAO.listarProductos();

        enviarRespuesta(
            exchange,
            200,
            gson.toJson(productos)
        );
    }

    /**
     * Busca un producto por su identificador.
     */
    private static void buscarProducto(
            HttpExchange exchange,
            int id) throws IOException {

        Producto producto =
            productoDAO.buscarProductoPorId(id);

        if (producto == null) {

            enviarRespuesta(
                exchange,
                404,
                "{\"mensaje\":\"Producto no encontrado\"}"
            );

            return;
        }

        enviarRespuesta(
            exchange,
            200,
            gson.toJson(producto)
        );
    }

    /**
     * Permite que React se conecte desde localhost:5173.
     */
    private static void configurarCors(
            HttpExchange exchange) {

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Origin",
            "http://localhost:5173"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    }

    /**
     * Envía una respuesta en formato JSON.
     */
    private static void enviarRespuesta(
            HttpExchange exchange,
            int codigo,
            String respuesta) throws IOException {

        byte[] contenido =
            respuesta.getBytes(StandardCharsets.UTF_8);

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