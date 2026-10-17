package routes;

import com.sun.net.httpserver.HttpServer;
import controllers.CategoriaController;

public final class CategoriaRoutes {

    private CategoriaRoutes() {
    }

    public static void registrar(HttpServer servidor) {

        servidor.createContext(
            "/api/categorias",
            CategoriaController::listar
        );
    }
}