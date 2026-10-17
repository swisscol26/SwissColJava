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
        mensaje.setRecipient(Message.RecipientType.TO, direccion);
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
}