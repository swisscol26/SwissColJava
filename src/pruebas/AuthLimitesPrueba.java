package pruebas;
import dto.LoginSolicitud;
import dto.RegistroSolicitud;
import middlewares.AuthValidator;
public class AuthLimitesPrueba {
    private static int total;
    private static void comprobar(boolean resultado) {
        if (!resultado) throw new AssertionError("Fallo en comprobación " + (total + 1));
        total++;
    }
    public static void main(String[] args) {
        LoginSolicitud login = new LoginSolicitud();
        login.setCorreo("a".repeat(88) + "@example.com");
        login.setPassword("a".repeat(128));
        comprobar(AuthValidator.validarLogin(login) == null);
        login.setPassword("a".repeat(129));
        comprobar(AuthValidator.validarLogin(login) != null);
        login.setPassword("Clave123");
        login.setCorreo("a".repeat(89) + "@example.com");
        comprobar(AuthValidator.validarLogin(login) != null);
        RegistroSolicitud registro = new RegistroSolicitud();
        registro.setNombre("Cliente Prueba");
        registro.setCorreo("cliente@example.com");
        registro.setPassword("Aa1" + "a".repeat(125));
        registro.setConfirmarPassword(registro.getPassword());
        comprobar(AuthValidator.validarRegistro(registro) == null);
        registro.setPassword("Aa1" + "a".repeat(126));
        registro.setConfirmarPassword(registro.getPassword());
        comprobar(AuthValidator.validarRegistro(registro) != null);
        registro.setPassword("Aa12345");
        registro.setConfirmarPassword(registro.getPassword());
        comprobar(AuthValidator.validarRegistro(registro) != null);
        System.out.println(total + " comprobaciones de límites aprobadas.");
    }
}
