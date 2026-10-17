package pruebas;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import routes.PedidoRoutes;
import routes.NotificacionRoutes;

/** Sin sesiones ni base de datos: comprueba origen y conserva la autenticación. */
public class OrigenLecturasPrueba {
    public static void main(String[] args) throws Exception {
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        PedidoRoutes.registrar(servidor);
        NotificacionRoutes.registrar(servidor);
        servidor.start();
        int pruebas = 0;
        try (var cliente = HttpClient.newHttpClient()) {
            String base = "http://127.0.0.1:" + servidor.getAddress().getPort();
            for (String ruta : new String[]{"/api/pedidos/pendientes", "/api/pedidos/mis-pedidos",
                    "/api/pedidos/estadisticas", "/api/notificaciones"}) {
                for (String origen : new String[]{null, "http://127.0.0.1:5500", "https://ajeno.example"}) {
                    var peticion = HttpRequest.newBuilder(URI.create(base + ruta)).GET();
                    if (origen != null) peticion.header("Origin", origen);
                    var respuesta = cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
                    int esperado = "https://ajeno.example".equals(origen) ? 403 : 401;
                    if (respuesta.statusCode() != esperado) throw new AssertionError(ruta + ": " + respuesta.statusCode());
                    if (origen == null && respuesta.headers().firstValue("Access-Control-Allow-Origin").isPresent())
                        throw new AssertionError("No inventar origen");
                    pruebas++;
                }
            }
            for (String ruta : new String[]{"/api/pedidos", "/api/pedidos/confirmar", "/api/pedidos/aprobar",
                    "/api/pedidos/costo-envio", "/api/pedidos/reportar-pago", "/api/notificaciones/leida"}) {
                var respuesta = cliente.send(HttpRequest.newBuilder(URI.create(base + ruta))
                    .POST(HttpRequest.BodyPublishers.ofString("{}")).build(), HttpResponse.BodyHandlers.ofString());
                if (respuesta.statusCode() != 403) throw new AssertionError("POST sin origen: " + ruta);
                pruebas++;
            }
            System.out.println("OrigenLecturas: " + pruebas + " comprobaciones HTTP aprobadas.");
        } finally {
            servidor.stop(0);
        }
    }
}
