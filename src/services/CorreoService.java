package services;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;

public class CorreoService {

    public void enviarAvisoEmpresa(
            String asunto,
            String contenido) throws MessagingException {

        enviar(System.getenv("SWISSCOL_MAIL_USER"), asunto, contenido);
    }

    public boolean configurado() {
        if (System.getenv("SWISSCOL_BREVO_API_KEY") != null) {
            return valor("SWISSCOL_BREVO_API_KEY") && valor("SWISSCOL_MAIL_USER");
        }
        if (System.getenv("SWISSCOL_RESEND_API_KEY") != null) {
            return valor("SWISSCOL_RESEND_API_KEY") && valor("SWISSCOL_MAIL_FROM");
        }
        return valor("SWISSCOL_MAIL_USER") && valor("SWISSCOL_MAIL_PASSWORD");
    }

    private boolean valor(String clave) {
        String valor = System.getenv(clave);
        return valor != null && !valor.isBlank();
    }

    public void enviar(String destinatario, String asunto, String contenido) throws MessagingException {
        if (System.getenv("SWISSCOL_BREVO_API_KEY") != null || System.getenv("SWISSCOL_RESEND_API_KEY") != null) {
            enviarHttps(destinatario, asunto, contenido);
            return;
        }

        String usuario = System.getenv("SWISSCOL_MAIL_USER");
        String password = System.getenv("SWISSCOL_MAIL_PASSWORD");

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalStateException(
                "No se encontró SWISSCOL_MAIL_USER."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "No se encontró SWISSCOL_MAIL_PASSWORD."
            );
        }

        usuario = usuario.trim();
        password = password.replaceAll("\\s+", "");

        Properties propiedades = new Properties();

        propiedades.setProperty("mail.smtp.host", "smtp.gmail.com");
        propiedades.setProperty("mail.smtp.port", "587");
        propiedades.setProperty("mail.smtp.auth", "true");

        propiedades.setProperty("mail.smtp.starttls.enable", "true");
        propiedades.setProperty("mail.smtp.starttls.required", "true");
        propiedades.setProperty("mail.smtp.ssl.checkserveridentity", "true");

        propiedades.setProperty("mail.smtp.connectiontimeout", "10000");
        propiedades.setProperty("mail.smtp.timeout", "15000");
        propiedades.setProperty("mail.smtp.writetimeout", "15000");

        Session sesionCorreo = Session.getInstance(propiedades);
        sesionCorreo.setDebug(false);

        InternetAddress direccion = new InternetAddress(usuario, true);
        direccion.validate();

        MimeMessage mensaje = new MimeMessage(sesionCorreo);

        mensaje.setFrom(direccion);
        InternetAddress receptor = new InternetAddress(destinatario, true);
        receptor.validate();
        mensaje.setRecipient(Message.RecipientType.TO, receptor);
        mensaje.setSubject(asunto, "UTF-8");
        mensaje.setText(contenido, "UTF-8");
        mensaje.setSentDate(new Date());

        try (Transport transporte =
                sesionCorreo.getTransport("smtp")) {

            transporte.connect(
                "smtp.gmail.com",
                587,
                usuario,
                password
            );

            transporte.sendMessage(
                mensaje,
                mensaje.getAllRecipients()
            );
        }
    }

    private void enviarHttps(String destinatario, String asunto, String contenido) throws MessagingException {
        if (!configurado()) throw new MessagingException("Configuración de correo incompleta.");
        try {
            boolean brevo = System.getenv("SWISSCOL_BREVO_API_KEY") != null;
            String json = new com.google.gson.Gson().toJson(brevo ? java.util.Map.of(
                "sender", java.util.Map.of("name", "SwissCol", "email", System.getenv("SWISSCOL_MAIL_USER")),
                "to", java.util.List.of(java.util.Map.of("email", destinatario)),
                "subject", asunto, "textContent", contenido
            ) : java.util.Map.of(
                "from", System.getenv("SWISSCOL_MAIL_FROM"), "to", java.util.List.of(destinatario),
                "subject", asunto, "text", contenido));
            var cliente = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10)).build();
            var peticion = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                    brevo ? "https://api.brevo.com/v3/smtp/email" : "https://api.resend.com/emails"))
                .timeout(java.time.Duration.ofSeconds(20))
                .header(brevo ? "api-key" : "Authorization", brevo ? System.getenv("SWISSCOL_BREVO_API_KEY") :
                    "Bearer " + System.getenv("SWISSCOL_RESEND_API_KEY"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json)).build();
            var respuesta = cliente.send(peticion, java.net.http.HttpResponse.BodyHandlers.discarding());
            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300)
                throw new MessagingException("El proveedor de correo rechazó el envío.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagingException("Envío interrumpido.");
        } catch (java.io.IOException e) {
            throw new MessagingException("No fue posible conectar con el proveedor de correo.");
        }
    }
}
