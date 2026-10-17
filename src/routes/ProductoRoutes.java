package routes;

import com.sun.net.httpserver.HttpServer;

import controllers.ProductoController;

public final class ProductoRoutes {

    private ProductoRoutes() {
        // Evita crear objetos de esta clase.
    }

    public static void registrar(HttpServer servidor) {

        servidor.createContext(
            "/api/productos",
            ProductoController::manejar
        );
    }
}
