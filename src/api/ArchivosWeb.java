package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Sirve exclusivamente el directorio público preparado durante el despliegue. */
public final class ArchivosWeb implements HttpHandler {
    private final Path raiz;
    private static final Map<String, String> TIPOS = Map.ofEntries(
        Map.entry("html", "text/html; charset=utf-8"),
        Map.entry("css", "text/css; charset=utf-8"),
        Map.entry("js", "text/javascript; charset=utf-8"),
        Map.entry("png", "image/png"), Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"), Map.entry("svg", "image/svg+xml"),
        Map.entry("webp", "image/webp"), Map.entry("ico", "image/x-icon"),
        Map.entry("woff", "font/woff"), Map.entry("woff2", "font/woff2"));

    public ArchivosWeb(Path raiz) throws IOException {
        this.raiz = raiz.toRealPath();
    }

    @Override
    public void handle(HttpExchange e) throws IOException {
        try {
            if (!e.getRequestMethod().equals("GET") && !e.getRequestMethod().equals("HEAD")) {
                e.getResponseHeaders().set("Allow", "GET, HEAD");
                e.sendResponseHeaders(405, -1);
                return;
            }
            String ruta = e.getRequestURI().getPath();
            // No resolver traversal, archivos ocultos, rutas de Windows ni APIs desconocidas.
            if (ruta == null || ruta.contains("\\") || ruta.contains(":" ) || ruta.indexOf(0) >= 0
                    || ruta.equals("/api") || ruta.startsWith("/api/")) {
                e.sendResponseHeaders(404, -1);
                return;
            }
            for (String segmento : ruta.split("/")) {
                if (segmento.startsWith(".")) {
                    e.sendResponseHeaders(404, -1);
                    return;
                }
            }
            String archivo = ruta.equals("/") ? "index.html"
                : (ruta.equals("/admin") || ruta.startsWith("/admin/"))
                    ? "admin/index.html" : ruta.substring(1);
            boolean permitido = archivo.equals("index.html") || archivo.equals("admin/index.html")
                || archivo.startsWith("css/") || archivo.startsWith("js/")
                || archivo.startsWith("img/") || archivo.startsWith("assets/");
            Path destino = raiz.resolve(archivo).normalize();
            String extension = archivo.substring(archivo.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
            if (!permitido || !TIPOS.containsKey(extension) || !destino.startsWith(raiz)
                    || !Files.isRegularFile(destino) || !destino.toRealPath().startsWith(raiz)) {
                e.sendResponseHeaders(404, -1);
                return;
            }
            e.getResponseHeaders().set("Content-Type", TIPOS.get(extension));
            e.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            e.getResponseHeaders().set("Cache-Control", "no-cache");
            long longitud = Files.size(destino);
            e.getResponseHeaders().set("Content-Length", Long.toString(longitud));
            if (e.getRequestMethod().equals("HEAD") || longitud == 0) {
                e.sendResponseHeaders(200, -1);
            } else {
                e.sendResponseHeaders(200, longitud);
                Files.copy(destino, e.getResponseBody());
            }
        } finally {
            e.close();
        }
    }
}
