package seguridad;

import java.time.Instant;
import java.util.Objects;

public class Sesion {

    private final int usuarioId;
    private final Instant expiraEn;

    public Sesion(int usuarioId, Instant expiraEn) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                "El identificador del usuario debe ser positivo."
            );
        }

        this.usuarioId = usuarioId;
        this.expiraEn = Objects.requireNonNull(expiraEn);
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public boolean estaVencida() {
        return !Instant.now().isBefore(expiraEn);
    }
}
