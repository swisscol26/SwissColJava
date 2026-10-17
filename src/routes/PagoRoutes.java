package routes;

import com.sun.net.httpserver.HttpServer;
import controllers.PagoConfigController;

public final class PagoRoutes {

    private PagoRoutes() {
    }

    public static void registrar(HttpServer servidor) {
        servidor.createContext(
            "/api/pagos/configuracion",
            PagoConfigController::obtener
        );
    }
}
