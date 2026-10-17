package services;

import dao.PedidoDAO;
import dto.PedidoResultado;
import dto.PedidoSolicitud;
import dto.PedidoHistorialResultado;
import dto.PedidoAdminResultado;
import dto.EstadisticasVentasResultado;
import seguridad.GestorSesiones;
import seguridad.Sesion;
import seguridad.AccesoException;
import java.util.List;

import java.sql.SQLException;

public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final CorreoService correoService = new CorreoService();

    private final NotificacionService notificacionService =
        new NotificacionService();

    private final AutorizacionService autorizacionService =
    new AutorizacionService();

    public PedidoResultado crear(
        String token,
        PedidoSolicitud solicitud) throws SQLException {

    Sesion sesion = GestorSesiones.obtenerSesion(token);

    if (sesion == null) {
        throw new SecurityException(
            "Debes iniciar sesión para registrar el pedido."
        );
    }

    validarDatosEnvio(solicitud);

    PedidoResultado resultado = pedidoDAO.crear(
        sesion.getUsuarioId(),
        solicitud
    );

    try {
        PedidoHistorialResultado pedido =
            pedidoDAO.listarPorUsuario(sesion.getUsuarioId())
                .stream()
                .filter(item ->
                    item.getPedidoId() == resultado.getPedidoId()
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "No se encontró el pedido para preparar el aviso."
                ));

        StringBuilder contenido = new StringBuilder();

        contenido.append("Nueva solicitud de compra SWISSCOL\n\n");
        contenido.append("Pedido: #")
            .append(pedido.getPedidoId()).append("\n");
        contenido.append("Cliente ID: ")
            .append(sesion.getUsuarioId()).append("\n");
        contenido.append("Fecha: ")
            .append(pedido.getFecha()).append("\n");
        contenido.append("Estado: ")
            .append(pedido.getEstado()).append("\n\n");
        contenido.append("Productos:\n");

        for (var producto : pedido.getProductos()) {
            contenido.append("- ")
                .append(producto.getNombre())
                .append(" | Cantidad: ")
                .append(producto.getCantidad())
                .append(" | Precio unitario: COP ")
                .append(producto.getPrecioUnitario().toPlainString())
                .append("\n");
        }

        contenido.append("\nTotal: COP ")
            .append(pedido.getTotal().toPlainString());

        contenido.append(
            "\n\nRevisa la solicitud en el panel del vendedor. " +
            "El inventario se descontará cuando confirmes el pedido."
        );

        correoService.enviarAvisoEmpresa(
            "SWISSCOL - Nueva solicitud #" + pedido.getPedidoId(),
            contenido.toString()
        );

    } catch (Exception e) {
        System.err.println(
            "Pedido #" + resultado.getPedidoId()
                + " guardado, pero no se pudo enviar el aviso. Tipo: "
                + e.getClass().getSimpleName()
        );
    }

    return resultado;
}

public void reportarPago(
        String token,
        dto.PagoReportadoSolicitud solicitud)
        throws SQLException {

    Sesion sesion = GestorSesiones.obtenerSesion(token);

    if (sesion == null) {
        throw new SecurityException(
            "Debes iniciar sesión para reportar el pago."
        );
    }

    if (solicitud == null
            || solicitud.getPedidoId() == null
            || solicitud.getPedidoId() <= 0) {

        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    String referencia = solicitud.getReferenciaPago() == null
        ? ""
        : solicitud.getReferenciaPago().trim();

    if (referencia.length() < 3
            || referencia.length() > 100) {

        throw new IllegalArgumentException(
            "Escribe una referencia de pago entre 3 y 100 caracteres."
        );
    }

    pedidoDAO.reportarPago(
        sesion.getUsuarioId(),
        solicitud.getPedidoId(),
        referencia
    );

    crearNotificacionSegura(
        solicitud.getPedidoId(),
        "Recibimos el reporte de pago de tu pedido #"
            + solicitud.getPedidoId()
            + ". El administrador verificará el pago antes de confirmar la venta.",
        "PAGO"
    );
}

    public List<PedidoHistorialResultado> listar(
        String token) throws SQLException {

    Sesion sesion = GestorSesiones.obtenerSesion(token);

    if (sesion == null) {
        throw new SecurityException(
            "Debes iniciar sesión para consultar tus pedidos."
        );
    }

    return pedidoDAO.listarPorUsuario(
        sesion.getUsuarioId()
    );
}

public PedidoResultado confirmar(
        String token,
        int pedidoId) throws SQLException {

    int codigo = autorizacionService.comprobarAdministrador(token);

    if (codigo == 401) {
        throw new AccesoException(
            401,
            "Debes iniciar sesión."
        );
    }

    if (codigo != 200) {
        throw new AccesoException(
            403,
            "Solo el administrador puede confirmar pedidos."
        );
    }

    PedidoResultado resultado = pedidoDAO.confirmar(pedidoId);

    crearNotificacionSegura(
        pedidoId,
        "Tu pedido #" + pedidoId
            + " fue confirmado. El pago fue verificado y el inventario quedó actualizado.",
        "PEDIDO"
    );

    return resultado;
}

public List<PedidoAdminResultado> listarPendientes(
        String token) throws SQLException {

    int codigo = autorizacionService.comprobarAdministrador(token);

    if (codigo == 401) {
        throw new AccesoException(
            401,
            "Debes iniciar sesión."
        );
    }

    if (codigo != 200) {
        throw new AccesoException(
            403,
            "Solo el administrador puede consultar los pedidos pendientes."
        );
    }

    return pedidoDAO.listarPendientes();
}

public EstadisticasVentasResultado consultarEstadisticas(
        String token) throws SQLException {

    int codigo =
        autorizacionService.comprobarAdministrador(token);

    if (codigo == 401) {
        throw new AccesoException(
            401,
            "Debes iniciar sesión."
        );
    }

    if (codigo != 200) {
        throw new AccesoException(
            403,
            "Solo el administrador puede consultar las estadísticas."
        );
    }

    return pedidoDAO.consultarEstadisticas();
}

private void validarDatosEnvio(PedidoSolicitud solicitud) {

    if (solicitud == null) {
        throw new IllegalArgumentException(
            "Debes enviar los datos del pedido."
        );
    }

    solicitud.setNombreDestinatario(validarTextoEnvio(
        solicitud.getNombreDestinatario(),
        "El nombre del destinatario",
        100,
        true
    ));

    String telefono = validarTextoEnvio(
        solicitud.getTelefonoDestinatario(),
        "El teléfono del destinatario",
        20,
        true
    );

    // Acepta números con prefijo internacional opcional,
    // espacios, paréntesis y guiones.
    if (!telefono.matches("\\+?[0-9()\\s-]+")) {
        throw new IllegalArgumentException(
            "El teléfono contiene caracteres no válidos."
        );
    }

    String digitos = telefono.replaceAll("[^0-9]", "");

    if (digitos.length() < 7 || digitos.length() > 15) {
        throw new IllegalArgumentException(
            "El teléfono debe contener entre 7 y 15 dígitos."
        );
    }

    solicitud.setTelefonoDestinatario(telefono);

    solicitud.setDepartamento(validarTextoEnvio(
        solicitud.getDepartamento(),
        "El departamento",
        100,
        true
    ));

    solicitud.setCiudad(validarTextoEnvio(
        solicitud.getCiudad(),
        "La ciudad",
        100,
        true
    ));

    solicitud.setDireccion(validarTextoEnvio(
        solicitud.getDireccion(),
        "La dirección",
        200,
        true
    ));

    solicitud.setBarrio(validarTextoEnvio(
        solicitud.getBarrio(),
        "El barrio",
        100,
        false
    ));

    solicitud.setIndicaciones(validarTextoEnvio(
        solicitud.getIndicaciones(),
        "Las indicaciones",
        500,
        false
    ));
}

private String validarTextoEnvio(
        String valor,
        String campo,
        int maximo,
        boolean obligatorio) {

    String texto = valor == null ? "" : valor.trim();

    if (texto.isEmpty()) {
        if (obligatorio) {
            throw new IllegalArgumentException(
                campo + ": este dato es obligatorio."
            );
        }

        return null;
    }

    if (texto.length() > maximo) {
        throw new IllegalArgumentException(
            campo + " admite máximo " + maximo + " caracteres."
        );
    }

    return texto;
}

private void validarCostoEnvio(
        dto.CostoEnvioSolicitud solicitud) {

    if (solicitud == null) {
        throw new IllegalArgumentException(
            "Debes enviar el pedido y el costo de envío."
        );
    }

    if (solicitud.getPedidoId() == null
            || solicitud.getPedidoId() <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    java.math.BigDecimal costo = solicitud.getCostoEnvio();

    if (costo == null) {
        throw new IllegalArgumentException(
            "Debes indicar el costo de envío. Usa 0 si es gratis."
        );
    }

    if (costo.signum() < 0) {
        throw new IllegalArgumentException(
            "El costo de envío no puede ser negativo."
        );
    }

    if (costo.stripTrailingZeros().scale() > 2) {
        throw new IllegalArgumentException(
            "El costo de envío admite máximo dos decimales."
        );
    }

    if (costo.compareTo(
            new java.math.BigDecimal("99999999.99")) > 0) {
        throw new IllegalArgumentException(
            "El costo de envío supera el valor permitido."
        );
    }
}

public void actualizarCostoEnvio(
        String token,
        dto.CostoEnvioSolicitud solicitud) throws SQLException {

    int codigo =
        autorizacionService.comprobarAdministrador(token);

    if (codigo == 401) {
        throw new AccesoException(
            401,
            "Debes iniciar sesión."
        );
    }

    if (codigo != 200) {
        throw new AccesoException(
            403,
            "Solo el administrador puede definir el costo de envío."
        );
    }

    validarCostoEnvio(solicitud);

    pedidoDAO.actualizarCostoEnvio(
        solicitud.getPedidoId(),
        solicitud.getCostoEnvio()
    );

    crearNotificacionSegura(
        solicitud.getPedidoId(),
        "Se definió el costo de envío de tu pedido #"
            + solicitud.getPedidoId()
            + " en COP "
            + solicitud.getCostoEnvio().stripTrailingZeros().toPlainString()
            + ".",
        "ENVIO"
    );
}

public void aprobarParaPago(
        String token,
        int pedidoId) throws SQLException {

    int codigo =
        autorizacionService.comprobarAdministrador(token);

    if (codigo == 401) {
        throw new AccesoException(
            401,
            "Debes iniciar sesión."
        );
    }

    if (codigo != 200) {
        throw new AccesoException(
            403,
            "Solo el administrador puede aprobar pedidos para pago."
        );
    }

    if (pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    java.math.BigDecimal totalPagar = pedidoDAO.aprobarParaPago(pedidoId);

    crearNotificacionSegura(
        pedidoId,
        config.PagoConfig.mensajeAprobacion(pedidoId, totalPagar),
        "PAGO"
    );
}


private void crearNotificacionSegura(
        int pedidoId,
        String mensaje,
        String tipo) {

    try {
        notificacionService.crearParaPedido(
            pedidoId,
            mensaje,
            tipo
        );
    } catch (Exception e) {
        System.err.println(
            "Pedido #" + pedidoId
                + " actualizado, pero no se pudo crear la notificación. Tipo: "
                + e.getClass().getSimpleName()
        );
    }
}

}
