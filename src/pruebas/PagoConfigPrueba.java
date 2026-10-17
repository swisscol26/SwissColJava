package pruebas;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import config.PagoConfig;
import routes.PagoRoutes;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Comprueba el endpoint real sin MySQL ni envíos de correo. */
public class PagoConfigPrueba {
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        PagoRoutes.registrar(servidor);
        servidor.start();
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            String base = "http://127.0.0.1:" + servidor.getAddress().getPort();
            URI uri = URI.create(base + "/api/pagos/configuracion");
            var respuesta = cliente.send(HttpRequest.newBuilder(uri)
                .header("Origin", "http://127.0.0.1:5500").GET().build(),
                HttpResponse.BodyHandlers.ofString());
            comprobar(respuesta.statusCode() == 200, "GET debe responder 200");
            var json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            comprobar(json.size() == 5, "Solo deben exponerse los datos públicos de pago");
            comprobar(json.get("numero").getAsString().equals(PagoConfig.getNequiNumero()), "Número centralizado");
            comprobar(json.get("titular").getAsString().equals(PagoConfig.getNequiTitular()), "Titular centralizado");
            comprobar(json.get("metodo").getAsString().equals("Nequi"), "Método");
            comprobar(json.get("moneda").getAsString().equals("COP"), "Moneda");
            comprobar(json.get("plazoHoras").getAsInt() == PagoConfig.PLAZO_HORAS, "Plazo");
            comprobar(respuesta.headers().firstValue("Access-Control-Allow-Origin").orElse("")
                .equals("http://127.0.0.1:5500"), "CORS tienda");
            comprobar(respuesta.headers().firstValue("Access-Control-Allow-Credentials").orElse("")
                .equals("true"), "Permitir fetch con credenciales");
            comprobar(respuesta.headers().firstValue("Cache-Control").orElse("").equals("no-store"), "Evitar datos obsoletos");
            var options = cliente.send(HttpRequest.newBuilder(uri).method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
            comprobar(options.statusCode() == 204, "OPTIONS");
            var post = cliente.send(HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
            comprobar(post.statusCode() == 405, "Rechaza POST");
            var invalida = cliente.send(HttpRequest.newBuilder(URI.create(uri + "/otra")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
            comprobar(invalida.statusCode() == 404, "Ruta exacta");
            String mensaje = PagoConfig.mensajeAprobacion(42, new BigDecimal("125500.75"));
            comprobar(mensaje.contains("$125.500,75 COP"), "Total sin redondear centavos");
            comprobar(mensaje.contains(PagoConfig.getNequiNumero()) && mensaje.contains(PagoConfig.getNequiTitular()), "Destinatario en notificación");
            comprobar(mensaje.contains("24 horas") && mensaje.contains("reportar la referencia"), "Instrucciones completas");
            System.out.println("PagoConfig: 16 comprobaciones aprobadas; GET real HTTP 200.");
        } finally {
            servidor.stop(0);
        }
    }
}
