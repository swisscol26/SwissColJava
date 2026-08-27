package routes;

import com.sun.net.httpserver.HttpServer;

import controllers.AuthController;

public final class AuthRoutes {

    private AuthRoutes() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Registra los endpoints de autenticación.
     */
    public static void registrar(HttpServer servidor) {

        servidor.createContext(
            "/api/auth/registro",
            AuthController::registrar
        );

        servidor.createContext(
            "/api/auth/login",
            AuthController::iniciarSesion
        );
    }
}