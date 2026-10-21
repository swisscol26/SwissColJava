package dao;

import conexion.Conexion;
import dto.DetallePedidoSolicitud;
import dto.PedidoResultado;
import dto.PedidoSolicitud;
import middlewares.PedidoValidator;
import dto.DetallePedidoResultado;
import dto.PedidoHistorialResultado;
import dto.PedidoAdminResultado;
import dto.EstadisticasVentasResultado;
import dto.ProductoVendidoResultado;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PedidoDAO {

    public PedidoResultado crear(
            int usuarioId,
            PedidoSolicitud solicitud) throws SQLException {

        String error = PedidoValidator.validar(solicitud);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                "El usuario no es válido."
            );
        }

        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {
                int pedidoId = insertarPedido(
                    conexion,
                    usuarioId,
                    solicitud
                );

                BigDecimal total = insertarDetalles(
                    conexion,
                    pedidoId,
                    solicitud
                );

                conexion.commit();

                return new PedidoResultado(
                    pedidoId,
                    "PENDIENTE",
                    total
                );

            } catch (SQLException | RuntimeException e) {

                try {
                    conexion.rollback();
                } catch (SQLException falloRollback) {
                    e.addSuppressed(falloRollback);
                }

                throw e;
            }
        }
    }

    private int insertarPedido(
        Connection conexion,
        int usuarioId,
        PedidoSolicitud solicitud) throws SQLException {

    String sql = """
        INSERT INTO orders (
            date,
            state,
            users_id,
            recipient_name,
            recipient_phone,
            shipping_department,
            shipping_city,
            shipping_address,
            shipping_neighborhood,
            shipping_notes
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try (PreparedStatement sentencia =
            conexion.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )) {

        sentencia.setDate(
            1,
            java.sql.Date.valueOf(
                java.time.LocalDate.now(
                    java.time.ZoneId.of("America/Bogota")
                )
            )
        );

        sentencia.setString(2, "PENDIENTE");
        sentencia.setInt(3, usuarioId);

        sentencia.setString(
            4, solicitud.getNombreDestinatario()
        );
        sentencia.setString(
            5, solicitud.getTelefonoDestinatario()
        );
        sentencia.setString(
            6, solicitud.getDepartamento()
        );
        sentencia.setString(
            7, solicitud.getCiudad()
        );
        sentencia.setString(
            8, solicitud.getDireccion()
        );
        sentencia.setString(
            9, solicitud.getBarrio()
        );
        sentencia.setString(
            10, solicitud.getIndicaciones()
        );

        if (sentencia.executeUpdate() != 1) {
            throw new SQLException(
                "No fue posible guardar el pedido."
            );
        }

        try (ResultSet claves = sentencia.getGeneratedKeys()) {

            if (!claves.next()) {
                throw new SQLException(
                    "No se pudo obtener el número del pedido."
                );
            }

            return claves.getInt(1);
        }
    }
}

    private BigDecimal insertarDetalles(
            Connection conexion,
            int pedidoId,
            PedidoSolicitud solicitud) throws SQLException {

        String consultarProducto = """
            SELECT price, offer_price, stock
            FROM product
            WHERE product_id = ?
            """;
        String insertarDetalle = """
            INSERT INTO order_detail
                (order_id, product_id, quantity, unitary_price)
            VALUES (?, ?, ?, ?)
            """;

        BigDecimal total = BigDecimal.ZERO;

        try (
            PreparedStatement consulta =
                conexion.prepareStatement(consultarProducto);

            PreparedStatement insercion =
                conexion.prepareStatement(insertarDetalle)
        ) {

            for (DetallePedidoSolicitud detalle :
                    solicitud.getProductos()) {

                consulta.setInt(1, detalle.getProductoId());

                BigDecimal precio;

                try (ResultSet producto =
                        consulta.executeQuery()) {

                    if (!producto.next()) {
                        throw new IllegalArgumentException(
                            "No existe el producto "
                                + detalle.getProductoId() + "."
                        );
                    }

                    if (detalle.getCantidad()
                            > producto.getInt("stock")) {

                        throw new IllegalArgumentException(
                            "Existencias insuficientes para el producto "
                                + detalle.getProductoId() + "."
                        );
                    }

                    BigDecimal precioNormal =
                        producto.getBigDecimal("price");

                    BigDecimal precioOferta =
                        producto.getBigDecimal("offer_price");

                    if (precioOferta != null) {
                        if (precioOferta.signum() <= 0
                                || precioOferta.compareTo(precioNormal) >= 0) {
                            throw new IllegalArgumentException(
                                "El producto " + detalle.getProductoId()
                                    + " tiene un precio de oferta inválido."
                            );
                        }

                        precio = precioOferta;
                    } else {
                        precio = precioNormal;
                    }
                }

                insercion.setInt(1, pedidoId);
                insercion.setInt(2, detalle.getProductoId());
                insercion.setInt(3, detalle.getCantidad());
                insercion.setBigDecimal(4, precio);

                insercion.executeUpdate();

                total = total.add(
                    precio.multiply(
                        BigDecimal.valueOf(detalle.getCantidad())
                    )
                );
            }
        }

        return total;
    }

 public List<PedidoHistorialResultado> listarPorUsuario(
        int usuarioId) throws SQLException {

    if (usuarioId <= 0) {
        throw new IllegalArgumentException(
            "El usuario no es válido."
        );
    }

    String sql = """
        SELECT
            o.order_id,
            o.date,
            o.state,
            o.shipping_cost,
            o.reservation_expires_at,
            o.payment_reported_at,
            o.payment_reference,
            d.product_id,
            p.name,
            d.quantity,
            d.unitary_price
        FROM orders o
        JOIN order_detail d ON d.order_id = o.order_id
        JOIN product p ON p.product_id = d.product_id
        WHERE o.users_id = ?
        ORDER BY o.order_id DESC, d.detail_id ASC
        """;

    List<PedidoHistorialResultado> pedidos =
        new ArrayList<>();

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement consulta =
            conexion.prepareStatement(sql)
    ) {
        consulta.setInt(1, usuarioId);

        try (ResultSet filas = consulta.executeQuery()) {
            Integer pedidoActual = null;
            String fecha = null;
            String estado = null;
            BigDecimal total = BigDecimal.ZERO;

            BigDecimal costoEnvio = null;
            String venceReserva = null;
            String pagoReportadoEn = null;
            String referenciaPago = null;

            List<DetallePedidoResultado> productos =
                new ArrayList<>();

            while (filas.next()) {
                int pedidoId = filas.getInt("order_id");

                if (pedidoActual != null
                        && pedidoActual.intValue() != pedidoId) {

                    PedidoHistorialResultado pedido =
                        new PedidoHistorialResultado(
                            pedidoActual,
                            fecha,
                            estado,
                            total,
                            productos
                        );

                    pedido.setCostoEnvio(costoEnvio);
                    pedido.setVenceReserva(venceReserva);
                    pedido.setPagoReportadoEn(pagoReportadoEn);
                    pedido.setReferenciaPago(referenciaPago);

                    pedidos.add(pedido);

                    productos = new ArrayList<>();
                    total = BigDecimal.ZERO;
                }

                pedidoActual = pedidoId;
                fecha = filas.getDate("date").toString();
                estado = filas.getString("state");

                costoEnvio = filas.getBigDecimal("shipping_cost");

                java.sql.Timestamp vencimiento =
                    filas.getTimestamp("reservation_expires_at");

                venceReserva = vencimiento == null
                    ? null
                    : vencimiento.toLocalDateTime().toString();

                java.sql.Timestamp pagoReportado =
                    filas.getTimestamp("payment_reported_at");

                pagoReportadoEn = pagoReportado == null
                    ? null
                    : pagoReportado.toLocalDateTime().toString();

                referenciaPago =
                    filas.getString("payment_reference");

                int cantidad = filas.getInt("quantity");

                BigDecimal precio =
                    filas.getBigDecimal("unitary_price");

                productos.add(
                    new DetallePedidoResultado(
                        filas.getInt("product_id"),
                        filas.getString("name"),
                        cantidad,
                        precio
                    )
                );

                total = total.add(
                    precio.multiply(BigDecimal.valueOf(cantidad))
                );
            }

            if (pedidoActual != null) {
                PedidoHistorialResultado pedido =
                    new PedidoHistorialResultado(
                        pedidoActual,
                        fecha,
                        estado,
                        total,
                        productos
                    );

                pedido.setCostoEnvio(costoEnvio);
                pedido.setVenceReserva(venceReserva);
                pedido.setPagoReportadoEn(pagoReportadoEn);
                pedido.setReferenciaPago(referenciaPago);

                pedidos.add(pedido);
            }
        }
    }

    return pedidos;
}

public PedidoResultado confirmar(
        int pedidoId) throws SQLException {

    if (pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    try (Connection conexion = Conexion.conectar()) {

        conexion.setAutoCommit(false);

        try {
            String consultarPedido = """
                SELECT state, shipping_cost
                FROM orders
                WHERE order_id = ?
                FOR UPDATE
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultarPedido)) {

                consulta.setInt(1, pedidoId);

                try (ResultSet pedido = consulta.executeQuery()) {

                    if (!pedido.next()) {
                        throw new IllegalArgumentException(
                            "El pedido no existe."
                        );
                    }

                    if (!"PAGO_POR_VERIFICAR".equals(
                            pedido.getString("state"))) {

                        throw new IllegalStateException(
                            "Solo se pueden confirmar pedidos " +
                            "con pago pendiente de verificación."
                        );
                    }

                    BigDecimal costoEnvio =
                        pedido.getBigDecimal("shipping_cost");

                    if (costoEnvio == null
                            || costoEnvio.signum() < 0) {

                        throw new IllegalStateException(
                            "El pedido no tiene un costo de envío válido."
                        );
                    }
                }
            }

            String consultarDetalles = """
                SELECT product_id, quantity, unitary_price
                FROM order_detail
                WHERE order_id = ?
                ORDER BY product_id, detail_id
                FOR UPDATE
                """;

            String descontarStock = """
                UPDATE product
                SET stock = stock - ?,
                    reserved_stock = reserved_stock - ?
                WHERE product_id = ?
                  AND stock >= ?
                  AND reserved_stock >= ?
                  AND stock >= reserved_stock
                """;

            BigDecimal total = BigDecimal.ZERO;
            int cantidadDetalles = 0;

            try (
                PreparedStatement detalles =
                    conexion.prepareStatement(consultarDetalles);

                PreparedStatement descuento =
                    conexion.prepareStatement(descontarStock)
            ) {

                detalles.setInt(1, pedidoId);

                try (ResultSet filas = detalles.executeQuery()) {

                    while (filas.next()) {

                        int productoId =
                            filas.getInt("product_id");

                        int cantidad =
                            filas.getInt("quantity");

                        BigDecimal precio =
                            filas.getBigDecimal("unitary_price");

                        if (cantidad <= 0
                                || precio == null
                                || precio.signum() <= 0) {

                            throw new IllegalStateException(
                                "El pedido contiene una cantidad " +
                                "o un precio inválido."
                            );
                        }

                        descuento.setInt(1, cantidad);
                        descuento.setInt(2, cantidad);
                        descuento.setInt(3, productoId);
                        descuento.setInt(4, cantidad);
                        descuento.setInt(5, cantidad);

                        if (descuento.executeUpdate() != 1) {
                            throw new IllegalStateException(
                                "No se pudo aplicar la reserva " +
                                "del producto " + productoId +
                                ". Revisa sus existencias."
                            );
                        }

                        total = total.add(
                            precio.multiply(
                                BigDecimal.valueOf(cantidad)
                            )
                        );

                        cantidadDetalles++;
                    }
                }
            }

            if (cantidadDetalles == 0) {
                throw new IllegalStateException(
                    "No se puede confirmar un pedido sin productos."
                );
            }

            String actualizarEstado = """
                UPDATE orders
                SET state = 'CONFIRMADO',
                    reservation_expires_at = NULL
                WHERE order_id = ?
                AND state = 'PAGO_POR_VERIFICAR'
                """;

            try (PreparedStatement actualizacion =
                    conexion.prepareStatement(actualizarEstado)) {

                actualizacion.setInt(1, pedidoId);

                if (actualizacion.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "No fue posible confirmar el pedido."
                    );
                }
            }

            conexion.commit();

            // Conservamos el total de productos usado actualmente.
            return new PedidoResultado(
                pedidoId,
                "CONFIRMADO",
                total
            );

        } catch (SQLException | RuntimeException e) {

            try {
                conexion.rollback();
            } catch (SQLException falloRollback) {
                e.addSuppressed(falloRollback);
            }

            throw e;
        }
    }
}

public List<PedidoAdminResultado> listarPendientes()
        throws SQLException {

    String sql = """
        SELECT
            o.order_id,
            o.date,
            o.state,
            o.users_id,
            o.recipient_name,
            o.recipient_phone,
            o.shipping_department,
            o.shipping_city,
            o.shipping_address,
            o.shipping_neighborhood,
            o.shipping_notes,
            o.shipping_cost,
            o.payment_reported_at,
            o.payment_reference,
            u.user_name,
            u.email,
            d.product_id,
            p.name,
            d.quantity,
            d.unitary_price
        FROM orders o
        JOIN users u ON u.user_id = o.users_id
        JOIN order_detail d ON d.order_id = o.order_id
        JOIN product p ON p.product_id = d.product_id
        WHERE o.state IN (
            'PENDIENTE',
            'PENDIENTE_PAGO',
            'PAGO_POR_VERIFICAR'
        )
        ORDER BY o.order_id ASC, d.detail_id ASC
        """;

    List<PedidoAdminResultado> pedidos = new ArrayList<>();

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement consulta =
            conexion.prepareStatement(sql);
        ResultSet filas = consulta.executeQuery()
    ) {

        Integer pedidoActual = null;
        String fecha = null;
        String estado = null;
        int usuarioId = 0;
        String nombreCliente = null;
        String correoCliente = null;

        String nombreDestinatario = null;
        String telefonoDestinatario = null;
        String departamento = null;
        String ciudad = null;
        String direccion = null;
        String barrio = null;
        String indicaciones = null;
        BigDecimal costoEnvio = null;
        String pagoReportadoEn = null;
        String referenciaPago = null;

        BigDecimal total = BigDecimal.ZERO;

        List<DetallePedidoResultado> productos =
            new ArrayList<>();

        while (filas.next()) {
            int pedidoId = filas.getInt("order_id");

            if (pedidoActual != null
                    && pedidoActual.intValue() != pedidoId) {

                PedidoAdminResultado pedido =
                    new PedidoAdminResultado(
                        pedidoActual,
                        fecha,
                        estado,
                        total,
                        productos,
                        usuarioId,
                        nombreCliente,
                        correoCliente
                    );

                pedido.setDatosEnvio(
                    nombreDestinatario,
                    telefonoDestinatario,
                    departamento,
                    ciudad,
                    direccion,
                    barrio,
                    indicaciones
                );



                pedido.setCostoEnvio(costoEnvio);
                pedido.setPagoReportadoEn(pagoReportadoEn);
                pedido.setReferenciaPago(referenciaPago);
                pedidos.add(pedido);

                productos = new ArrayList<>();
                total = BigDecimal.ZERO;
            }

            pedidoActual = pedidoId;
            fecha = filas.getDate("date").toString();
            estado = filas.getString("state");
            usuarioId = filas.getInt("users_id");
            nombreCliente = filas.getString("user_name");
            correoCliente = filas.getString("email");

            nombreDestinatario =
                filas.getString("recipient_name");
            telefonoDestinatario =
                filas.getString("recipient_phone");
            departamento =
                filas.getString("shipping_department");
            ciudad =
                filas.getString("shipping_city");
            direccion =
                filas.getString("shipping_address");
            barrio =
                filas.getString("shipping_neighborhood");
            indicaciones =
                filas.getString("shipping_notes");

            costoEnvio = filas.getBigDecimal("shipping_cost");

            java.sql.Timestamp pagoReportado =
                filas.getTimestamp("payment_reported_at");

            pagoReportadoEn = pagoReportado == null
                ? null
                : pagoReportado.toLocalDateTime().toString();

            referenciaPago = filas.getString("payment_reference");

            int cantidad = filas.getInt("quantity");

            BigDecimal precio =
                filas.getBigDecimal("unitary_price");

            productos.add(
                new DetallePedidoResultado(
                    filas.getInt("product_id"),
                    filas.getString("name"),
                    cantidad,
                    precio
                )
            );

            total = total.add(
                precio.multiply(BigDecimal.valueOf(cantidad))
            );
        }

        if (pedidoActual != null) {
            PedidoAdminResultado pedido =
                new PedidoAdminResultado(
                    pedidoActual,
                    fecha,
                    estado,
                    total,
                    productos,
                    usuarioId,
                    nombreCliente,
                    correoCliente
                );

            pedido.setDatosEnvio(
                nombreDestinatario,
                telefonoDestinatario,
                departamento,
                ciudad,
                direccion,
                barrio,
                indicaciones
            );
            pedido.setCostoEnvio(costoEnvio);
            pedido.setPagoReportadoEn(pagoReportadoEn);
            pedido.setReferenciaPago(referenciaPago);
            pedidos.add(pedido);
        }
    }

    return pedidos;
}

public EstadisticasVentasResultado consultarEstadisticas()
        throws SQLException {

    String sql = """
        SELECT
            o.order_id,
            d.product_id,
            p.name,
            d.quantity,
            d.unitary_price
        FROM orders o
        LEFT JOIN order_detail d ON d.order_id = o.order_id
        LEFT JOIN product p ON p.product_id = d.product_id
        WHERE o.state = 'CONFIRMADO'
        ORDER BY o.order_id, d.detail_id
        """;

    long pedidosConfirmados = 0;
    Integer pedidoAnterior = null;
    BigDecimal totalVendido = BigDecimal.ZERO;

    java.util.Map<Integer, ProductoVendidoResultado> acumulados =
        new java.util.HashMap<>();

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement consulta =
            conexion.prepareStatement(sql);
        ResultSet filas = consulta.executeQuery()
    ) {

        while (filas.next()) {
            int pedidoId = filas.getInt("order_id");

            if (pedidoAnterior == null
                    || pedidoAnterior.intValue() != pedidoId) {
                pedidosConfirmados++;
                pedidoAnterior = pedidoId;
            }

            int productoId = filas.getInt("product_id");

            if (filas.wasNull()) {
                continue;
            }

            long cantidad = filas.getLong("quantity");

            BigDecimal subtotal = filas
                .getBigDecimal("unitary_price")
                .multiply(BigDecimal.valueOf(cantidad));

            totalVendido = totalVendido.add(subtotal);

            ProductoVendidoResultado anterior =
                acumulados.get(productoId);

            long unidades = cantidad;
            BigDecimal totalProducto = subtotal;

            if (anterior != null) {
                unidades += anterior.getUnidadesVendidas();
                totalProducto = totalProducto.add(
                    anterior.getTotalVendido()
                );
            }

            String nombre = filas.getString("name");

            acumulados.put(
                productoId,
                new ProductoVendidoResultado(
                    productoId,
                    nombre != null
                        ? nombre
                        : "Producto #" + productoId,
                    unidades,
                    totalProducto
                )
            );
        }
    }

    List<ProductoVendidoResultado> productosMasVendidos =
        new ArrayList<>(acumulados.values());

    productosMasVendidos.sort(
        java.util.Comparator
            .comparingLong(
                ProductoVendidoResultado::getUnidadesVendidas
            )
            .reversed()
            .thenComparingInt(
                ProductoVendidoResultado::getProductoId
            )
    );

    return new EstadisticasVentasResultado(
        pedidosConfirmados,
        totalVendido,
        productosMasVendidos
    );
}

public void actualizarCostoEnvio(
        int pedidoId,
        BigDecimal costoEnvio) throws SQLException {

    String sql = """
        UPDATE orders
        SET shipping_cost = ?
        WHERE order_id = ?
          AND state = 'PENDIENTE'
        """;

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement sentencia =
            conexion.prepareStatement(sql)
    ) {
        sentencia.setBigDecimal(1, costoEnvio);
        sentencia.setInt(2, pedidoId);

        if (sentencia.executeUpdate() != 1) {
            throw new IllegalStateException(
                "No se pudo actualizar el envío. " +
                "Verifica que el pedido exista y siga pendiente."
            );
        }
    }
}

public BigDecimal aprobarParaPago(int pedidoId) throws SQLException {

    if (pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    try (Connection conexion = Conexion.conectar()) {

        conexion.setAutoCommit(false);

        try {
            BigDecimal totalPagar = BigDecimal.ZERO;
            String consultarPedido = """
                SELECT state, shipping_cost, shipping_address
                FROM orders
                WHERE order_id = ?
                FOR UPDATE
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultarPedido)) {

                consulta.setInt(1, pedidoId);

                try (ResultSet pedido = consulta.executeQuery()) {

                    if (!pedido.next()) {
                        throw new IllegalArgumentException(
                            "El pedido no existe."
                        );
                    }

                    if (!"PENDIENTE".equals(
                            pedido.getString("state"))) {

                        throw new IllegalStateException(
                            "Solo se pueden aprobar pedidos pendientes."
                        );
                    }

                    BigDecimal envio =
                        pedido.getBigDecimal("shipping_cost");

                    if (envio == null || envio.signum() < 0) {
                        throw new IllegalStateException(
                            "Debes guardar un costo de envío válido."
                        );
                    }

                    totalPagar = envio;

                    String direccion =
                        pedido.getString("shipping_address");

                    if (direccion == null || direccion.isBlank()) {
                        throw new IllegalStateException(
                            "El pedido no tiene dirección de envío."
                        );
                    }
                }
            }

            String consultarDetalles = """
                SELECT product_id, quantity, unitary_price
                FROM order_detail
                WHERE order_id = ?
                ORDER BY product_id, detail_id
                FOR UPDATE
                """;

            String reservarStock = """
                UPDATE product
                SET reserved_stock = reserved_stock + ?
                WHERE product_id = ?
                  AND reserved_stock >= 0
                  AND stock - reserved_stock >= ?
                """;

            int cantidadDetalles = 0;

            try (
                PreparedStatement detalles =
                    conexion.prepareStatement(consultarDetalles);

                PreparedStatement reserva =
                    conexion.prepareStatement(reservarStock)
            ) {

                detalles.setInt(1, pedidoId);

                try (ResultSet filas = detalles.executeQuery()) {

                    while (filas.next()) {
                        int productoId =
                            filas.getInt("product_id");

                        int cantidad =
                            filas.getInt("quantity");

                        if (cantidad <= 0) {
                            throw new IllegalStateException(
                                "El pedido contiene una cantidad inválida."
                            );
                        }

                        reserva.setInt(1, cantidad);
                        reserva.setInt(2, productoId);
                        reserva.setInt(3, cantidad);

                        if (reserva.executeUpdate() != 1) {
                            throw new IllegalStateException(
                                "No hay unidades disponibles suficientes " +
                                "para el producto " + productoId + "."
                            );
                        }

                        totalPagar = totalPagar.add(filas.getBigDecimal("unitary_price")
                            .multiply(BigDecimal.valueOf(cantidad)));
                        cantidadDetalles++;
                    }
                }
            }

            if (cantidadDetalles == 0) {
                throw new IllegalStateException(
                    "No se puede aprobar un pedido sin productos."
                );
            }

            java.time.LocalDateTime vencimiento =
                java.time.LocalDateTime.now(
                    java.time.ZoneId.of("America/Bogota")
                ).plusHours(config.PagoConfig.PLAZO_HORAS);

            String actualizarPedido = """
                UPDATE orders
                SET state = 'PENDIENTE_PAGO',
                    reservation_expires_at = ?
                WHERE order_id = ?
                  AND state = 'PENDIENTE'
                """;

            try (PreparedStatement actualizacion =
                    conexion.prepareStatement(actualizarPedido)) {

                actualizacion.setTimestamp(
                    1,
                    java.sql.Timestamp.valueOf(vencimiento)
                );
                actualizacion.setInt(2, pedidoId);

                if (actualizacion.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "No fue posible aprobar el pedido."
                    );
                }
            }

            conexion.commit();
            return totalPagar;

        } catch (SQLException | RuntimeException e) {

            try {
                conexion.rollback();
            } catch (SQLException falloRollback) {
                e.addSuppressed(falloRollback);
            }

            throw e;
        }
    }
}

public boolean cancelarSolicitud(
        int pedidoId) throws SQLException {

    if (pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    try (Connection conexion = Conexion.conectar()) {

        conexion.setAutoCommit(false);

        try {
            boolean tieneReserva;
            String consultarPedido = """
                SELECT state
                FROM orders
                WHERE order_id = ?
                FOR UPDATE
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultarPedido)) {

                consulta.setInt(1, pedidoId);

                try (ResultSet pedido = consulta.executeQuery()) {

                    if (!pedido.next()) {
                        throw new IllegalArgumentException(
                            "El pedido no existe."
                        );
                    }

                    String estado = pedido.getString("state");
                    if ("CANCELADO".equals(estado)) {
                        conexion.rollback();
                        return false;
                    }
                    if (estado == null || !java.util.Set.of("PENDIENTE", "PENDIENTE_PAGO", "PAGO_POR_VERIFICAR").contains(estado)) {
                        throw new IllegalStateException("Solo se pueden cancelar solicitudes pendientes. Los pedidos confirmados no se pueden cancelar aquí.");
                    }
                    tieneReserva = !"PENDIENTE".equals(estado);
                }
            }

            if (tieneReserva) {
                String consultarDetalles = """
                    SELECT product_id, quantity
                    FROM order_detail
                    WHERE order_id = ?
                    ORDER BY product_id, detail_id
                    FOR UPDATE
                    """;

                String liberarStock = """
                    UPDATE product
                    SET reserved_stock = reserved_stock - ?
                    WHERE product_id = ?
                      AND reserved_stock >= ?
                    """;

                int cantidadDetalles = 0;

                try (
                    PreparedStatement detalles =
                        conexion.prepareStatement(consultarDetalles);

                    PreparedStatement liberacion =
                        conexion.prepareStatement(liberarStock)
                ) {

                    detalles.setInt(1, pedidoId);

                    try (ResultSet filas = detalles.executeQuery()) {

                        while (filas.next()) {
                            int productoId = filas.getInt("product_id");
                            int cantidad = filas.getInt("quantity");

                            if (cantidad <= 0) {
                                throw new IllegalStateException(
                                    "El pedido contiene una cantidad inválida."
                                );
                            }

                            liberacion.setInt(1, cantidad);
                            liberacion.setInt(2, productoId);
                            liberacion.setInt(3, cantidad);

                            if (liberacion.executeUpdate() != 1) {
                                throw new IllegalStateException(
                                    "No se pudo liberar la reserva del producto "
                                        + productoId + "."
                                );
                            }

                            cantidadDetalles++;
                        }
                    }
                }

                if (cantidadDetalles == 0) {
                    throw new IllegalStateException(
                        "El pedido no contiene productos."
                    );
                }

            }

            String actualizarPedido = """
                UPDATE orders
                SET state = 'CANCELADO',
                    reservation_expires_at = NULL
                WHERE order_id = ?
                  AND state IN ('PENDIENTE', 'PENDIENTE_PAGO', 'PAGO_POR_VERIFICAR')
                """;

            try (PreparedStatement actualizacion =
                    conexion.prepareStatement(actualizarPedido)) {

                actualizacion.setInt(1, pedidoId);

                if (actualizacion.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "No fue posible cancelar la solicitud."
                    );
                }
            }

            conexion.commit();
            return true;

        } catch (SQLException | RuntimeException e) {

            try {
                conexion.rollback();
            } catch (SQLException falloRollback) {
                e.addSuppressed(falloRollback);
            }

            throw e;
        }
    }
}

public boolean liberarReservaVencida(
        int pedidoId) throws SQLException {

    if (pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El número del pedido no es válido."
        );
    }

    try (Connection conexion = Conexion.conectar()) {

        conexion.setAutoCommit(false);

        try {
            String consultarPedido = """
                SELECT state, reservation_expires_at
                FROM orders
                WHERE order_id = ?
                FOR UPDATE
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultarPedido)) {

                consulta.setInt(1, pedidoId);

                try (ResultSet pedido = consulta.executeQuery()) {

                    if (!pedido.next()) {
                        throw new IllegalArgumentException(
                            "El pedido no existe."
                        );
                    }

                    if (!"PENDIENTE_PAGO".equals(
                            pedido.getString("state"))) {

                        conexion.rollback();
                        return false;
                    }

                    java.sql.Timestamp fechaLimite =
                        pedido.getTimestamp("reservation_expires_at");

                    if (fechaLimite == null) {
                        throw new IllegalStateException(
                            "El pedido no tiene fecha de vencimiento."
                        );
                    }

                    java.time.LocalDateTime ahora =
                        java.time.LocalDateTime.now(
                            java.time.ZoneId.of("America/Bogota")
                        );

                    if (ahora.isBefore(fechaLimite.toLocalDateTime())) {
                        conexion.rollback();
                        return false;
                    }
                }
            }

            String consultarDetalles = """
                SELECT product_id, quantity
                FROM order_detail
                WHERE order_id = ?
                ORDER BY product_id, detail_id
                FOR UPDATE
                """;

            String liberarStock = """
                UPDATE product
                SET reserved_stock = reserved_stock - ?
                WHERE product_id = ?
                  AND reserved_stock >= ?
                """;

            int cantidadDetalles = 0;

            try (
                PreparedStatement detalles =
                    conexion.prepareStatement(consultarDetalles);

                PreparedStatement liberacion =
                    conexion.prepareStatement(liberarStock)
            ) {

                detalles.setInt(1, pedidoId);

                try (ResultSet filas = detalles.executeQuery()) {

                    while (filas.next()) {
                        int productoId = filas.getInt("product_id");
                        int cantidad = filas.getInt("quantity");

                        if (cantidad <= 0) {
                            throw new IllegalStateException(
                                "El pedido contiene una cantidad inválida."
                            );
                        }

                        liberacion.setInt(1, cantidad);
                        liberacion.setInt(2, productoId);
                        liberacion.setInt(3, cantidad);

                        if (liberacion.executeUpdate() != 1) {
                            throw new IllegalStateException(
                                "No se pudo liberar la reserva del producto "
                                    + productoId + "."
                            );
                        }

                        cantidadDetalles++;
                    }
                }
            }

            if (cantidadDetalles == 0) {
                throw new IllegalStateException(
                    "El pedido no contiene productos."
                );
            }

            String actualizarPedido = """
                UPDATE orders
                SET state = 'VENCIDO',
                    reservation_expires_at = NULL
                WHERE order_id = ?
                  AND state = 'PENDIENTE_PAGO'
                """;

            try (PreparedStatement actualizacion =
                    conexion.prepareStatement(actualizarPedido)) {

                actualizacion.setInt(1, pedidoId);

                if (actualizacion.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "No fue posible marcar el pedido como vencido."
                    );
                }
            }

            conexion.commit();
            return true;

        } catch (SQLException | RuntimeException e) {

            try {
                conexion.rollback();
            } catch (SQLException falloRollback) {
                e.addSuppressed(falloRollback);
            }

            throw e;
        }
    }
}

public List<Integer> listarReservasVencidas()
        throws SQLException {

    String sql = """
        SELECT order_id
        FROM orders
        WHERE state = 'PENDIENTE_PAGO'
          AND reservation_expires_at <= ?
        ORDER BY reservation_expires_at, order_id
        LIMIT 100
        """;

    List<Integer> pedidos = new ArrayList<>();

    java.time.LocalDateTime ahora =
        java.time.LocalDateTime.now(
            java.time.ZoneId.of("America/Bogota")
        );

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement consulta =
            conexion.prepareStatement(sql)
    ) {

        consulta.setTimestamp(
            1,
            java.sql.Timestamp.valueOf(ahora)
        );

        try (ResultSet filas = consulta.executeQuery()) {

            while (filas.next()) {
                pedidos.add(filas.getInt("order_id"));
            }
        }
    }

    return pedidos;
}

public void reportarPago(
        int usuarioId,
        int pedidoId,
        String referenciaPago) throws SQLException {

    if (usuarioId <= 0 || pedidoId <= 0) {
        throw new IllegalArgumentException(
            "El pedido o el usuario no son válidos."
        );
    }

    try (Connection conexion = Conexion.conectar()) {
        conexion.setAutoCommit(false);

        try {
            String consultarPedido = """
                SELECT state, reservation_expires_at
                FROM orders
                WHERE order_id = ?
                  AND users_id = ?
                FOR UPDATE
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultarPedido)) {

                consulta.setInt(1, pedidoId);
                consulta.setInt(2, usuarioId);

                try (ResultSet pedido = consulta.executeQuery()) {
                    if (!pedido.next()) {
                        throw new IllegalArgumentException(
                            "No se encontró ese pedido en tu cuenta."
                        );
                    }

                    String estado = pedido.getString("state");

                    if (!"PENDIENTE_PAGO".equals(estado)) {
                        throw new IllegalStateException(
                            "Este pedido no está esperando un pago."
                        );
                    }

                    java.sql.Timestamp vencimiento =
                        pedido.getTimestamp(
                            "reservation_expires_at"
                        );

                    if (vencimiento == null) {
                        throw new IllegalStateException(
                            "El pedido no tiene un plazo de pago válido."
                        );
                    }

                    java.time.LocalDateTime ahora =
                        java.time.LocalDateTime.now(
                            java.time.ZoneId.of("America/Bogota")
                        );

                    if (!ahora.isBefore(
                            vencimiento.toLocalDateTime())) {

                        throw new IllegalStateException(
                            "El plazo de pago venció. " +
                            "La reserva ya no puede confirmarse."
                        );
                    }
                }
            }

            String actualizarPedido = """
                UPDATE orders
                SET state = 'PAGO_POR_VERIFICAR',
                    payment_reported_at = ?,
                    payment_reference = ?
                WHERE order_id = ?
                  AND users_id = ?
                  AND state = 'PENDIENTE_PAGO'
                """;

            try (PreparedStatement actualizacion =
                    conexion.prepareStatement(actualizarPedido)) {

                java.time.LocalDateTime ahora =
                    java.time.LocalDateTime.now(
                        java.time.ZoneId.of("America/Bogota")
                    );

                actualizacion.setTimestamp(
                    1,
                    java.sql.Timestamp.valueOf(ahora)
                );
                actualizacion.setString(2, referenciaPago);
                actualizacion.setInt(3, pedidoId);
                actualizacion.setInt(4, usuarioId);

                if (actualizacion.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "No fue posible registrar el pago."
                    );
                }
            }

            conexion.commit();

        } catch (SQLException | RuntimeException e) {
            try {
                conexion.rollback();
            } catch (SQLException falloRollback) {
                e.addSuppressed(falloRollback);
            }

            throw e;
        }
    }
}
}