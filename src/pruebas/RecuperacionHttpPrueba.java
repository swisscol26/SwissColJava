package pruebas;

import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import routes.*;

public class RecuperacionHttpPrueba {
    public static void main(String[] args) throws Exception {
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AuthRoutes.registrar(servidor); PedidoRoutes.registrar(servidor); servidor.start();
        String base = "http://127.0.0.1:" + servidor.getAddress().getPort();
        String origen = config.ServidorConfig.origenes().iterator().next();
        int pruebas = 0;
        try (var cliente = HttpClient.newHttpClient()) {
            for (String cookie : new String[]{"", "swisscol_sesion=falsa"}) {
                var req = HttpRequest.newBuilder(URI.create(base + "/api/pedidos"))
                    .header("Origin",origen).header("Content-Type","application/json")
                    .header("Cookie",cookie).POST(HttpRequest.BodyPublishers.ofString("{}"));
                if (cliente.send(req.build(),HttpResponse.BodyHandlers.ofString()).statusCode() != 401)
                    throw new AssertionError("Compra sin sesión permitida");
                pruebas++;
            }
            for (String ruta : new String[]{"recuperar", "restablecer"}) {
                for (String fuente : new String[]{origen,"https://ajeno.example"}) {
                    var req = HttpRequest.newBuilder(URI.create(base + "/api/auth/" + ruta))
                        .header("Origin",fuente).header("Content-Type","application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{}"));
                    var respuesta = cliente.send(req.build(),HttpResponse.BodyHandlers.ofString());
                    if (respuesta.statusCode() != (fuente.equals(origen) ? 400 : 403))
                        throw new AssertionError("Validación de recuperación " + respuesta.statusCode());
                    if (!respuesta.headers().firstValue("Cache-Control").orElse("").equals("no-store"))
                        throw new AssertionError("Falta no-store");
                    pruebas++;
                }
            }
            System.out.println("Recuperación HTTP: " + pruebas + " comprobaciones correctas.");
        } finally { servidor.stop(0); }
    }
}
