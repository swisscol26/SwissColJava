package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
    private static final String RUTA_BASE = "/api/productos";

    private static final Gson gson = new Gson();
    private static final ProductoDAO productoDAO = new ProductoDAO();

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(PUERTO),
            0
        );

        servidor.createContext(
            RUTA_BASE,
            ProductoApi::manejarPeticion
        );

        servidor.setExecutor(null);
        servidor.start();

        System.out.println(
            "API de productos ejecutándose en http://localhost:"
                + PUERTO + RUTA_BASE
        );
    }

    /**
     * Atiende las solicitudes HTTP del CRUD de productos.
     */
    private static void manejarPeticion(HttpExchange exchange)
            throws IOException {

        configurarCors(exchange);

        String metodo = exchange.getRequestMethod();
        String ruta = exchange.getRequestURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {

            Integer id = obtenerIdDesdeRuta(ruta);

            switch (metodo.toUpperCase()) {

                case "GET":

                    if (id == null) {
                        listarProductos(exchange);
                    } else {
                        buscarProducto(exchange, id);
                    }

                    break;

                case "POST":

                    if (id != null) {
                        enviarMensaje(
                            exchange,
                            400,
                            "No incluya un ID en la ruta para registrar."
                        );
                        return;
                    }

                    registrarProducto(exchange);
                    break;

                case "PUT":

                    if (id == null) {
                        enviarMensaje(
                            exchange,
                            400,
                            "Debe indicar el ID del producto."
                        );
                        return;
                    }

                    actualizarProducto(exchange, id);
                    break;

                case "DELETE":

                    if (id == null) {
                        enviarMensaje(
                            exchange,
                            400,
                            "Debe indicar el ID del producto."
                        );
                        return;
                    }

                    eliminarProducto(exchange, id);
                    break;

                default:

                    enviarMensaje(
                        exchange,
                        405,
                        "Método no permitido."
                    );
            }

        } catch (NumberFormatException e) {

            enviarMensaje(
                exchange,
                400,
                "El ID debe ser un número entero."
            );

        } catch (JsonSyntaxException e) {

            enviarMensaje(
                exchange,
                400,
                "El contenido JSON no es válido."
            );

        } catch (Exception e) {

            System.out.println(
                "Error en la API: " + e.getMessage()
            );

            enviarMensaje(
                exchange,
                500,
                "Error interno del servidor."
            );
        }
    }

    /**
     * Devuelve todos los productos.
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
     * Devuelve un producto según su identificador.
     */
    private static void buscarProducto(
            HttpExchange exchange,
            int id) throws IOException {

        Producto producto =
            productoDAO.buscarProductoPorId(id);

        if (producto == null) {

            enviarMensaje(
                exchange,
                404,
                "Producto no encontrado."
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
     * Registra un producto recibido en formato JSON.
     */
    private static void registrarProducto(
            HttpExchange exchange) throws IOException {

        Producto producto = leerProducto(exchange);

        String error = validarProducto(producto, true);

        if (error != null) {
            enviarMensaje(exchange, 400, error);
            return;
        }

        Producto existente =
            productoDAO.buscarProductoPorId(
                producto.getProductId()
            );

        if (existente != null) {

            enviarMensaje(
                exchange,
                409,
                "Ya existe un producto con ese ID."
            );

            return;
        }

        boolean registrado =
            productoDAO.insertarProducto(producto);

        if (!registrado) {

            enviarMensaje(
                exchange,
                400,
                "No fue posible registrar el producto. "
                    + "Verifique la categoría."
            );

            return;
        }

        enviarRespuesta(
            exchange,
            201,
            gson.toJson(producto)
        );
    }

    /**
     * Actualiza un producto utilizando el ID de la ruta.
     */
    private static void actualizarProducto(
            HttpExchange exchange,
            int id) throws IOException {

        Producto existente =
            productoDAO.buscarProductoPorId(id);

        if (existente == null) {

            enviarMensaje(
                exchange,
                404,
                "Producto no encontrado."
            );

            return;
        }

        Producto producto = leerProducto(exchange);
        producto.setProductId(id);

        String error = validarProducto(producto, false);

        if (error != null) {
            enviarMensaje(exchange, 400, error);
            return;
        }

        boolean actualizado =
            productoDAO.actualizarProducto(producto);

        if (!actualizado) {

            enviarMensaje(
                exchange,
                400,
                "No fue posible actualizar el producto."
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
     * Elimina un producto según su identificador.
     */
    private static void eliminarProducto(
            HttpExchange exchange,
            int id) throws IOException {

        Producto existente =
            productoDAO.buscarProductoPorId(id);

        if (existente == null) {

            enviarMensaje(
                exchange,
                404,
                "Producto no encontrado."
            );

            return;
        }

        boolean eliminado =
            productoDAO.eliminarProducto(id);

        if (!eliminado) {

            enviarMensaje(
                exchange,
                400,
                "No fue posible eliminar el producto."
            );

            return;
        }

        enviarMensaje(
            exchange,
            200,
            "Producto eliminado correctamente."
        );
    }

    /**
     * Convierte el cuerpo JSON de la petición en un Producto.
     */
    private static Producto leerProducto(
            HttpExchange exchange) throws IOException {

        byte[] contenido =
            exchange.getRequestBody().readAllBytes();

        String json = new String(
            contenido,
            StandardCharsets.UTF_8
        );

        Producto producto =
            gson.fromJson(json, Producto.class);

        if (producto == null) {
            throw new JsonSyntaxException(
                "Producto vacío."
            );
        }

        return producto;
    }

    /**
     * Valida los datos requeridos por la tabla product.
     */
    private static String validarProducto(
            Producto producto,
            boolean validarId) {

        if (validarId && producto.getProductId() <= 0) {
            return "El ID debe ser mayor que cero.";
        }

        if (producto.getName() == null
                || producto.getName().isBlank()) {
            return "El nombre es obligatorio.";
        }

        if (producto.getName().length() > 45) {
            return "El nombre admite máximo 45 caracteres.";
        }

        if (producto.getPrice() < 0) {
            return "El precio no puede ser negativo.";
        }

        if (producto.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }

        if (producto.getImage() != null
                && producto.getImage().length() > 45) {
            return "La imagen admite máximo 45 caracteres.";
        }

        if (producto.getCategoryId() <= 0) {
            return "La categoría es obligatoria.";
        }

        return null;
    }

    /**
     * Obtiene el identificador incluido en la URL.
     */
    private static Integer obtenerIdDesdeRuta(
            String ruta) {

        if (ruta.equals(RUTA_BASE)
                || ruta.equals(RUTA_BASE + "/")) {
            return null;
        }

        if (!ruta.startsWith(RUTA_BASE + "/")) {
            throw new NumberFormatException();
        }

        String idTexto = ruta.substring(
            (RUTA_BASE + "/").length()
        );

        return Integer.parseInt(idTexto);
    }

    /**
     * Permite las peticiones enviadas desde React.
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
     * Crea un mensaje sencillo en formato JSON.
     */
    private static void enviarMensaje(
            HttpExchange exchange,
            int codigo,
            String mensaje) throws IOException {

        String json = gson.toJson(
            new MensajeRespuesta(mensaje)
        );

        enviarRespuesta(exchange, codigo, json);
    }

    /**
     * Envía una respuesta JSON al cliente.
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

    /**
     * Estructura utilizada para responder mensajes JSON.
     */
    private static class MensajeRespuesta {

        private final String mensaje;

        public MensajeRespuesta(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}