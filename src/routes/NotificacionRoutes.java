package routes;

import com.sun.net.httpserver.HttpServer;
import controllers.NotificacionController;

public final class NotificacionRoutes {

    private NotificacionRoutes() {
    }

    public static void registrar(HttpServer servidor) {

        servidor.createContext(
            "/api/notificaciones",
            NotificacionController::listar
        );

        servidor.createContext(
            "/api/notificaciones/leida",
            NotificacionController::marcarComoLeida
        );
    }
}
