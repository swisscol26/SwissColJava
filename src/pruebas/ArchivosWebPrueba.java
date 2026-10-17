package pruebas;

import api.ArchivosWeb;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Pruebas HTTP aisladas: no inicia la API ni se conecta a MySQL. */
public final class ArchivosWebPrueba {
    public static void main(String[] args) throws Exception {
        Path raiz = Files.createTempDirectory("swisscol-web-test-");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            Files.createDirectories(raiz.resolve("admin"));
            Files.createDirectories(raiz.resolve("css"));
            Files.writeString(raiz.resolve("index.html"), "TIENDA");
            Files.writeString(raiz.resolve("admin/index.html"), "PANEL");
            Files.writeString(raiz.resolve("css/style.css"), "body{}");
            Files.writeString(raiz.resolve(".env"), "PRIVADO");
            Files.writeString(raiz.resolve("css/datos.sql"), "PRIVADO");
            server.createContext("/", new ArchivosWeb(raiz));
            server.start();
            try (HttpClient cliente = HttpClient.newHttpClient()) {
                String base = "http://127.0.0.1:" + server.getAddress().getPort();
                comprobar(cliente, base, "/", "GET", 200, "TIENDA");
                comprobar(cliente, base, "/admin/pedidos", "GET", 200, "PANEL");
                comprobar(cliente, base, "/css/style.css?v=2", "GET", 200, "body{}");
                comprobar(cliente, base, "/", "HEAD", 200, "");
                comprobar(cliente, base, "/", "POST", 405, "");
                for (String ruta : new String[]{"/.env", "/%2e%2e/index.html", "/css/../index.html",
                    "/css/datos.sql", "/api/no-existe", "/no-existe", "/admin/../.env", "/css/%5c.env"}) {
                    comprobar(cliente, base, ruta, "GET", 404, "");
                }
            }
            System.out.println("ArchivosWeb: 13 comprobaciones HTTP aprobadas.");
        } finally {
            server.stop(0);
            try (var archivos = Files.walk(raiz)) {
                for (Path archivo : archivos.sorted(Comparator.reverseOrder()).toList()) Files.delete(archivo);
            }
        }
    }
    private static void comprobar(HttpClient cliente, String base, String ruta,
            String metodo, int estado, String cuerpo) throws Exception {
        var respuesta = cliente.send(HttpRequest.newBuilder(URI.create(base + ruta))
            .method(metodo, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != estado || !respuesta.body().equals(cuerpo)) {
            throw new AssertionError(metodo + " " + ruta + ": " + respuesta.statusCode());
        }
        if (estado == 200 && !respuesta.headers().firstValue("X-Content-Type-Options").orElse("").equals("nosniff")) {
            throw new AssertionError("Falta nosniff");
        }
    }
}
