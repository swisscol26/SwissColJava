package routes;

import com.sun.net.httpserver.HttpServer;
import controllers.PedidoController;

public final class PedidoRoutes {

    private PedidoRoutes() {
    }

    public static void registrar(HttpServer servidor) {

        servidor.createContext(
            "/api/pedidos",
            PedidoController::crear
        );

        servidor.createContext(
            "/api/pedidos/mis-pedidos",
            PedidoController::listar
        );

        servidor.createContext("/api/pedidos/cancelar", PedidoController::cancelarSolicitud);

        servidor.createContext(
            "/api/pedidos/confirmar",
            PedidoController::confirmar
        );

        servidor.createContext(
            "/api/pedidos/pendientes",
            PedidoController::listarPendientes
        );

        servidor.createContext(
            "/api/pedidos/estadisticas",
            PedidoController::consultarEstadisticas
        );

                servidor.createContext(
            "/api/pedidos/costo-envio",
            PedidoController::actualizarCostoEnvio
        );

                servidor.createContext(
            "/api/pedidos/aprobar",
            PedidoController::aprobarParaPago
        );

                servidor.createContext(
            "/api/pedidos/reportar-pago",
            PedidoController::reportarPago
        );
    }
}