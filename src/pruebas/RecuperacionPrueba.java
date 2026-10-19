package pruebas;

import dao.UsuarioDAO;
import modelo.Usuario;
import services.*;
import seguridad.*;
import java.time.*;

public class RecuperacionPrueba {
    static int comprobaciones;
    static class Reloj extends Clock {
        long tiempo = 1800000000000L;
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId z) { return this; }
        public Instant instant() { return Instant.ofEpochMilli(tiempo); }
    }
    static class Usuarios extends UsuarioDAO {
        Usuario usuario = new Usuario();
        Usuarios() { usuario.setUserId(71); usuario.setPasswordHash(PasswordUtil.crearHash("Anterior123")); }
        public Usuario buscarPorCorreo(String correo) { return correo.equals("cliente@example.com") ? usuario : null; }
        public boolean cambiarPassword(int id, String anterior, String nuevo) {
            if (id != 71 || !anterior.equals(usuario.getPasswordHash())) return false;
            usuario.setPasswordHash(nuevo); return true;
        }
    }
    static class Correo extends CorreoService {
        String codigo; int enviados;
        public boolean configurado() { return true; }
        public void enviar(String destino, String asunto, String cuerpo) {
            comprobar(destino.equals("cliente@example.com"), "Destinatario correcto");
            codigo = cuerpo.split("\n\n")[1]; enviados++;
        }
    }
    static void comprobar(boolean ok, String caso) {
        if (!ok) throw new AssertionError(caso);
        comprobaciones++;
    }
    interface Accion { void ejecutar() throws Exception; }
    static void rechaza(Accion a) throws Exception {
        try { a.ejecutar(); throw new AssertionError("Debía rechazar"); }
        catch (IllegalArgumentException | SecurityException esperado) { comprobaciones++; }
    }
    public static void main(String[] args) throws Exception {
        var dao = new Usuarios(); var mail = new Correo(); var reloj = new Reloj();
        var servicio = new RecuperacionService(dao, mail, reloj, Runnable::run);
        servicio.solicitar("desconocido@example.com");
        comprobar(mail.enviados == 0, "Cuenta inexistente no recibe correo");
        servicio.solicitar(" CLIENTE@example.com ");
        comprobar(mail.codigo.length() == 43, "Código aleatorio largo");
        servicio.solicitar("cliente@example.com");
        comprobar(mail.enviados == 1, "Límite de reenvíos");
        String token = mail.codigo;
        rechaza(() -> servicio.restablecer("x".repeat(43), "NuevaClave123", "NuevaClave123"));
        rechaza(() -> servicio.restablecer(token, "debil", "debil"));
        rechaza(() -> servicio.restablecer(token, "NuevaClave123", "Distinta123"));
        String sesion = GestorSesiones.crearSesion(71);
        String otra = GestorSesiones.crearSesion(72);
        servicio.restablecer(token, "NuevaClave123", "NuevaClave123");
        comprobar(PasswordUtil.verificar("NuevaClave123", dao.usuario.getPasswordHash()), "Contraseña nueva funciona");
        comprobar(!PasswordUtil.verificar("Anterior123", dao.usuario.getPasswordHash()), "Anterior deja de funcionar");
        comprobar(GestorSesiones.obtenerSesion(sesion) == null, "Revoca sesiones");
        comprobar(GestorSesiones.obtenerSesion(otra) != null, "Conserva sesiones ajenas");
        rechaza(() -> servicio.restablecer(token, "OtraClave123", "OtraClave123"));
        reloj.tiempo += 600001;
        servicio.solicitar("cliente@example.com");
        reloj.tiempo += 900001;
        rechaza(() -> servicio.restablecer(mail.codigo, "OtraClave123", "OtraClave123"));
        reloj.tiempo += 600001;
        servicio.solicitar("cliente@example.com");
        dao.usuario.setPasswordHash(PasswordUtil.crearHash("CambioExterno123"));
        rechaza(() -> servicio.restablecer(mail.codigo, "OtraClave123", "OtraClave123"));
        rechaza(() -> new PedidoService().crear(null, null));
        rechaza(() -> new PedidoService().crear("sesion-falsificada", null));
        String vencida = GestorSesiones.crearSesion(71); GestorSesiones.cerrarSesion(vencida);
        rechaza(() -> new PedidoService().crear(vencida, null));
        System.out.println("Recuperación y compras: " + comprobaciones + " comprobaciones correctas. Sin DB ni correos reales.");
    }
}
