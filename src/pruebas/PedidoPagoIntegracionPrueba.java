package pruebas;

import conexion.Conexion;
import dao.PedidoDAO;
import dao.NotificacionDAO;
import dto.PagoReportadoSolicitud;
import seguridad.GestorSesiones;
import services.PedidoService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Integración optativa: crea únicamente datos sintéticos y los retira en finally. No envía correo. */
public class PedidoPagoIntegracionPrueba {
    private static int comprobaciones;
    private static void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
        comprobaciones++;
    }
    private static int insertar(Connection c, String sql, Object... valores) throws Exception {
        try (var s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) s.setObject(i + 1, valores[i]);
            s.executeUpdate();
            try (var r = s.getGeneratedKeys()) { return r.next() ? r.getInt(1) : 0; }
        }
    }
    private static void ejecutar(Connection c, String sql, Object... valores) throws Exception {
        try (var s = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) s.setObject(i + 1, valores[i]);
            s.executeUpdate();
        }
    }
    private static void inventario(Connection c, int producto, int stock, int reserva) throws Exception {
        try (var s = c.prepareStatement("SELECT stock, reserved_stock FROM product WHERE product_id=?")) {
            s.setInt(1, producto);
            try (var r = s.executeQuery()) {
                comprobar(r.next() && r.getInt(1) == stock && r.getInt(2) == reserva,
                    "Inventario esperado: " + stock + ", reserva: " + reserva);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"--ejecutar".equals(args[0])) {
            System.out.println("Usa --ejecutar para crear y retirar datos sintéticos en MySQL.");
            return;
        }
        String marca = UUID.randomUUID().toString();
        int cliente = 0, admin = 0;
        int producto = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000000000, 2000000000);
        boolean productoCreado = false;
        String tokenCliente = null, tokenAdmin = null;
        List<Integer> pedidos = new ArrayList<>();
        try (Connection c = Conexion.conectar()) {
            try {
                cliente = insertar(c, "INSERT INTO users(user_name,email,password,user_role) VALUES(?,?,?,?)",
                    "Prueba pago SwissCol", "pago-" + marca + "@example.invalid", UUID.randomUUID().toString(), "CUSTOMER");
                admin = insertar(c, "INSERT INTO users(user_name,email,password,user_role) VALUES(?,?,?,?)",
                    "Prueba admin SwissCol", "admin-" + marca + "@example.invalid", UUID.randomUUID().toString(), "ADMIN");
                int categoria;
                try (var s = c.createStatement(); var r = s.executeQuery("SELECT category_id FROM category LIMIT 1")) {
                    if (!r.next()) throw new IllegalStateException("Se necesita una categoría existente.");
                    categoria = r.getInt(1);
                }
                ejecutar(c, "INSERT INTO product(product_id,name,price,stock,category_id) VALUES(?,?,?,?,?)",
                    producto, "PRUEBA PAGO " + marca.substring(0, 8), new BigDecimal("10000.25"), 10, categoria);
                productoCreado = true;
                for (int i = 0; i < 2; i++) {
                    int pedido = insertar(c, "INSERT INTO orders(date,state,users_id,shipping_address,shipping_cost) VALUES(CURRENT_DATE,'PENDIENTE',?,?,?)",
                        cliente, "Dirección sintética de prueba", new BigDecimal("5000.25"));
                    pedidos.add(pedido);
                    ejecutar(c, "INSERT INTO order_detail(order_id,product_id,quantity,unitary_price) VALUES(?,?,?,?)",
                        pedido, producto, 2, new BigDecimal("10000.25"));
                }
                tokenCliente = GestorSesiones.crearSesion(cliente);
                tokenAdmin = GestorSesiones.crearSesion(admin);
                PedidoService servicio = new PedidoService();
                PedidoDAO dao = new PedidoDAO();
                int pedido = pedidos.get(0);
                boolean rechazo = false;
                try { servicio.aprobarParaPago(tokenCliente, pedido); }
                catch (seguridad.AccesoException e) { rechazo = true; }
                comprobar(rechazo, "Cliente no puede aprobar");
                servicio.aprobarParaPago(tokenAdmin, pedido);
                inventario(c, producto, 10, 2);
                var aprobado = dao.listarPorUsuario(cliente).stream().filter(p -> p.getPedidoId() == pedido).findFirst().orElseThrow();
                comprobar("PENDIENTE_PAGO".equals(aprobado.getEstado()), "Estado aprobado");
                comprobar(aprobado.getVenceReserva() != null, "Plazo guardado");
                var avisos = new NotificacionDAO().listarPorUsuario(cliente);
                comprobar(avisos.stream().anyMatch(n -> n.getMensaje().contains("$25.000,75 COP")
                    && n.getMensaje().contains(config.PagoConfig.getNequiNumero())), "Notificación con total y número");
                rechazo = false;
                try { dao.confirmar(pedido); } catch (IllegalStateException e) { rechazo = true; }
                comprobar(rechazo, "No se confirma antes del reporte");
                PagoReportadoSolicitud reporte = new PagoReportadoSolicitud();
                reporte.setPedidoId(pedido);
                reporte.setReferenciaPago("  REF-" + marca + "  ");
                servicio.reportarPago(tokenCliente, reporte);
                var reportado = dao.listarPorUsuario(cliente).stream().filter(p -> p.getPedidoId() == pedido).findFirst().orElseThrow();
                comprobar("PAGO_POR_VERIFICAR".equals(reportado.getEstado()), "Estado reportado");
                comprobar(("REF-" + marca).equals(reportado.getReferenciaPago()), "Referencia persistida sin espacios");
                comprobar(reportado.getPagoReportadoEn() != null, "Fecha del reporte persistida");
                var paraAdmin = servicio.listarPendientes(tokenAdmin).stream().filter(p -> p.getPedidoId() == pedido).findFirst().orElseThrow();
                comprobar(paraAdmin.getReferenciaPago().equals(reportado.getReferenciaPago())
                    && paraAdmin.getPagoReportadoEn().equals(reportado.getPagoReportadoEn()), "Administrador recibe referencia y fecha");
                comprobar(paraAdmin.getTotal().add(paraAdmin.getCostoEnvio()).compareTo(new BigDecimal("25000.75")) == 0,
                    "Total final de administrador");
                comprobar(new NotificacionDAO().listarPorUsuario(cliente).size() == 2, "Notificación del reporte");
                inventario(c, producto, 10, 2);
                rechazo = false;
                try { servicio.reportarPago(tokenCliente, reporte); } catch (IllegalStateException e) { rechazo = true; }
                comprobar(rechazo, "Reporte duplicado rechazado");
                servicio.confirmar(tokenAdmin, pedido);
                inventario(c, producto, 8, 0);
                comprobar(new NotificacionDAO().listarPorUsuario(cliente).size() == 3, "Notificación de confirmación");
                int vencido = pedidos.get(1);
                servicio.aprobarParaPago(tokenAdmin, vencido);
                inventario(c, producto, 8, 2);
                ejecutar(c, "UPDATE orders SET reservation_expires_at='2000-01-01 00:00:00' WHERE order_id=?", vencido);
                comprobar(dao.listarReservasVencidas().contains(vencido), "Reserva vencida detectable");
                comprobar(dao.liberarReservaVencida(vencido), "Libera reserva vencida");
                inventario(c, producto, 8, 0);
                comprobar(!dao.liberarReservaVencida(vencido), "Liberación idempotente");
                System.out.println("PedidoPagoIntegracion: " + comprobaciones + " comprobaciones aprobadas.");
            } finally {
                GestorSesiones.cerrarSesion(tokenCliente);
                GestorSesiones.cerrarSesion(tokenAdmin);
                c.setAutoCommit(false);
                try {
                    if (cliente != 0) ejecutar(c, "DELETE FROM notifications WHERE users_id=?", cliente);
                    for (int pedido : pedidos) {
                        ejecutar(c, "DELETE FROM order_detail WHERE order_id=?", pedido);
                        ejecutar(c, "DELETE FROM orders WHERE order_id=?", pedido);
                    }
                    if (productoCreado) ejecutar(c, "DELETE FROM product WHERE product_id=?", producto);
                    if (cliente != 0) ejecutar(c, "DELETE FROM users WHERE user_id=?", cliente);
                    if (admin != 0) ejecutar(c, "DELETE FROM users WHERE user_id=?", admin);
                    c.commit();
                    System.out.println("Datos sintéticos retirados.");
                } catch (Exception e) {
                    c.rollback();
                    throw e;
                }
            }
        }
    }
}
