package api;

import com.sun.net.httpserver.HttpServer;

import routes.AuthRoutes;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServidorApi {

    private static final int PUERTO = 8080;

    public static void main(String[] args)
            throws IOException {

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(PUERTO),
            0
        );

        servidor.createContext(
            "/api/productos",
            ProductoApi::manejarPeticion
        );

        AuthRoutes.registrar(servidor);

        servidor.setExecutor(null);
        servidor.start();

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