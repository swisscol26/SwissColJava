package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import controllers.ProductoController;
import routes.ProductoRoutes;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class ProductoApi {

    private static final int PUERTO = 8080;

    private ProductoApi() {
        // Evita crear objetos de esta clase.
    }

    public static void main(String[] args)
            throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(PUERTO),
            0
        );

        ProductoRoutes.registrar(servidor);

        servidor.setExecutor(null);
        servidor.start();

        System.out.println(
            "API de productos ejecutándose en "
                + "http://localhost:" + PUERTO
                + "/api/productos"
        );
    }

    public static void manejarPeticion(
            HttpExchange exchange) throws IOException {

        ProductoController.manejar(exchange);
    }
}
