package routes;

import com.sun.net.httpserver.HttpServer;

import controllers.AuthController;
import services.AutorizacionService;

public final class AuthRoutes {

    private AuthRoutes() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Registra los endpoints de autenticación.
     */
    public static void registrar(HttpServer servidor) {

        servidor.createContext("/api/auth/recuperar", controllers.RecuperacionController::atender);
        servidor.createContext("/api/auth/restablecer", controllers.RecuperacionController::atender);

        servidor.createContext(
            "/api/auth/registro",
            AuthController::registrar
        );

        servidor.createContext(
            "/api/auth/login",
            AuthController::iniciarSesion
        );

        servidor.createContext(
            "/api/auth/logout",
            AuthController::cerrarSesion
        );

        servidor.createContext(
            "/api/auth/sesion",
            AuthController::verificarSesion
        );

        servidor.createContext(
            "/api/auth/admin",
            AuthController::verificarAdministrador
        );

    }
}
