package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import services.RecuperacionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class RecuperacionController {
    private static final Gson GSON = new Gson();
    private static final RecuperacionService SERVICE = new RecuperacionService();
    private record Solicitud(String correo, String codigo, String password, String confirmarPassword) {}

    public static void atender(HttpExchange e) throws IOException {
        e.getResponseHeaders().set("Cache-Control", "no-store");
        String origen = e.getRequestHeaders().getFirst("Origin");
        if (origen == null || !config.ServidorConfig.origenes().contains(origen)) {
            responder(e, 403, "Origen de la solicitud no permitido."); return;
        }
        e.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
        e.getResponseHeaders().set("Vary", "Origin");
        e.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        e.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equals(e.getRequestMethod())) { e.sendResponseHeaders(204,-1); e.close(); return; }
        if (!"POST".equals(e.getRequestMethod())) { responder(e,405,"Método no permitido."); return; }
        String ruta = e.getRequestURI().getPath();
        if (!ruta.equals("/api/auth/recuperar") && !ruta.equals("/api/auth/restablecer")) {
            responder(e,404,"Ruta no encontrada."); return;
        }
        String tipo = e.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.split(";",2)[0].trim().equalsIgnoreCase("application/json")) {
            responder(e,415,"Envía los datos en formato JSON."); return;
        }
        byte[] contenido = e.getRequestBody().readNBytes(4097);
        if (contenido.length > 4096) { responder(e,413,"Solicitud demasiado grande."); return; }
        try {
            Solicitud s = GSON.fromJson(new String(contenido, StandardCharsets.UTF_8), Solicitud.class);
            if (s == null) throw new IllegalArgumentException("Completa los datos.");
            if (ruta.endsWith("/recuperar")) {
                SERVICE.solicitar(s.correo());
                responder(e,202,"Si el correo pertenece a una cuenta, recibirás un código. Revisa también spam. Si ya lo solicitaste, espera 10 minutos antes de pedir otro.");
            } else {
                SERVICE.restablecer(s.codigo(),s.password(),s.confirmarPassword());
                responder(e,200,"Contraseña actualizada. Inicia sesión con tu nueva contraseña.");
            }
        } catch (JsonParseException ex) { responder(e,400,"El contenido JSON no es válido."); }
        catch (IllegalArgumentException ex) { responder(e,400,ex.getMessage()); }
        catch (IllegalStateException ex) { responder(e,503,"La recuperación no está disponible temporalmente. Intenta más tarde."); }
        catch (Exception ex) {
            System.err.println("Error de recuperación. Tipo: " + ex.getClass().getSimpleName());
            responder(e,500,"No fue posible completar la recuperación.");
        }
    }

    private static void responder(HttpExchange e, int estado, String mensaje) throws IOException {
        byte[] bytes = GSON.toJson(Map.of("exito",estado < 300,"mensaje",mensaje)).getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        e.sendResponseHeaders(estado,bytes.length);
        try (var salida = e.getResponseBody()) { salida.write(bytes); }
    }
}
