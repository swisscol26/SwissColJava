package services;

import dao.UsuarioDAO;
import seguridad.GestorSesiones;
import seguridad.PasswordUtil;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

/** Códigos de un uso para la instancia única, nunca se devuelven por HTTP ni se registran. */
public class RecuperacionService {
    private record Codigo(int usuario, String passwordAnterior, long vence) {}
    private final Map<String, Codigo> codigos = new HashMap<>();
    private final Map<String, Long> solicitudes = new HashMap<>();
    private final UsuarioDAO usuarios;
    private final CorreoService correo;
    private final Clock reloj;
    private final Executor executor;
    private final SecureRandom random = new SecureRandom();
    private long ventana;
    private int cantidad;

    public RecuperacionService() {
        this(new UsuarioDAO(), new CorreoService(), Clock.systemUTC(),
            new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(30), r -> {
                Thread t = new Thread(r, "recuperacion-correo"); t.setDaemon(true); return t;
            }));
    }

    public RecuperacionService(UsuarioDAO usuarios, CorreoService correo, Clock reloj, Executor executor) {
        this.usuarios = usuarios; this.correo = correo; this.reloj = reloj; this.executor = executor;
    }

    public synchronized void solicitar(String email) {
        if (email == null || email.length() > 100 || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Escribe un correo electrónico válido.");
        if (!correo.configurado()) throw new IllegalStateException("El servicio de recuperación no está disponible. Intenta más tarde.");
        long ahora = reloj.millis();
        solicitudes.entrySet().removeIf(e -> e.getValue() <= ahora);
        codigos.entrySet().removeIf(e -> e.getValue().vence() <= ahora);
        String destino = email.trim().toLowerCase(Locale.ROOT);
        // El mismo límite y respuesta se aplican a cuentas existentes y desconocidas.
        if (solicitudes.containsKey(destino)) return;
        if (ahora - ventana >= 60000) { ventana = ahora; cantidad = 0; }
        if (cantidad >= 30 || solicitudes.size() >= 2000)
            throw new IllegalStateException("Hay muchas solicitudes. Intenta nuevamente más tarde.");
        solicitudes.put(destino, ahora + 600000); cantidad++;
        try { executor.execute(() -> emitir(destino)); }
        catch (RejectedExecutionException e) {
            throw new IllegalStateException("Hay muchas solicitudes. Intenta nuevamente más tarde.");
        }
    }

    private void emitir(String destino) {
        String clave = null;
        try {
            var usuario = usuarios.buscarPorCorreo(destino);
            if (usuario == null) return;
            byte[] bytes = new byte[32]; random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            clave = hash(token);
            synchronized (this) {
                codigos.entrySet().removeIf(e -> e.getValue().usuario() == usuario.getUserId());
                codigos.put(clave, new Codigo(usuario.getUserId(), usuario.getPasswordHash(), reloj.millis() + 900000));
            }
            correo.enviar(destino, "SwissCol: recupera tu contraseña",
                "Tu código de recuperación es:\n\n" + token +
                "\n\nEn SwissCol abre Mi cuenta > Olvidé mi contraseña > Ya tengo un código. " +
                "Pega el código y elige una contraseña nueva. Caduca en 15 minutos y solo puede usarse una vez. " +
                "Si no solicitaste este cambio, ignora el mensaje. No compartas el código.");
        } catch (Exception e) {
            synchronized (this) { if (clave != null) codigos.remove(clave); }
            System.err.println("No se pudo enviar la recuperación. Tipo: " + e.getClass().getSimpleName());
        }
    }

    public synchronized void restablecer(String token, String password, String confirmacion) throws java.sql.SQLException {
        if (password == null || password.length() < 8 || password.length() > 128 ||
                !password.matches("(?s).*[A-Z].*") || !password.matches("(?s).*[a-z].*") || !password.matches("(?s).*[0-9].*"))
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres, mayúscula, minúscula y número.");
        if (!password.equals(confirmacion)) throw new IllegalArgumentException("Las contraseñas no coinciden.");
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalido();
        String clave = hash(token);
        Codigo codigo = codigos.get(clave);
        if (codigo == null || codigo.vence() <= reloj.millis()) { codigos.remove(clave); throw invalido(); }
        boolean cambiado = usuarios.cambiarPassword(codigo.usuario(), codigo.passwordAnterior(), PasswordUtil.crearHash(password));
        codigos.remove(clave);
        if (!cambiado) throw invalido();
        codigos.entrySet().removeIf(e -> e.getValue().usuario() == codigo.usuario());
        GestorSesiones.cerrarSesionesUsuario(codigo.usuario());
    }

    private IllegalArgumentException invalido() {
        return new IllegalArgumentException("El código no es válido o venció. Solicita uno nuevo.");
    }

    private static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
