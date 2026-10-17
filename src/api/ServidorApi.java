package api;

import com.sun.net.httpserver.HttpServer;

import routes.AuthRoutes;
import routes.ProductoRoutes;
import routes.PedidoRoutes;
import routes.CategoriaRoutes;
import routes.NotificacionRoutes;
import routes.PagoRoutes;
import services.ReservaService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServidorApi {

    private static final int PUERTO = config.ServidorConfig.puerto();

    public static void main(String[] args)
            throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(
                config.ServidorConfig.entorno("SWISSCOL_BIND_ADDRESS", "0.0.0.0"), PUERTO),
            0
        );

        ProductoRoutes.registrar(servidor);
        AuthRoutes.registrar(servidor);
        PedidoRoutes.registrar(servidor);
        CategoriaRoutes.registrar(servidor);
        NotificacionRoutes.registrar(servidor);
        PagoRoutes.registrar(servidor);

        servidor.setExecutor(null);
        servidor.start();

        ReservaService reservaService = new ReservaService();

        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                reservaService.detener();
                servidor.stop(1);
            }, "swisscol-cierre")
        );

        reservaService.iniciar();

        System.out.println(
            "API Swisscol ejecutándose en "
                + "http://localhost:" + PUERTO
        );

        System.out.println(
            "Productos: /api/productos"
        );

        System.out.println(
            "Registro: /api/auth/registro"
        );

        System.out.println(
            "Login: /api/auth/login"
        );
    }
}
