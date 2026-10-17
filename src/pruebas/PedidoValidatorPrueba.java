package pruebas;

import dto.DetallePedidoSolicitud;
import dto.PedidoSolicitud;
import middlewares.PedidoValidator;

import java.util.List;

public class PedidoValidatorPrueba {

    public static void main(String[] args) {

        comprobar(
            "Solicitud nula",
            PedidoValidator.validar(null) != null
        );

        PedidoSolicitud pedido = new PedidoSolicitud();
        pedido.setProductos(List.of());

        comprobar(
            "Pedido vacío",
            PedidoValidator.validar(pedido) != null
        );

        DetallePedidoSolicitud detalle = new DetallePedidoSolicitud();
        detalle.setProductoId(16);
        detalle.setCantidad(0);

        pedido.setProductos(List.of(detalle));

        comprobar(
            "Cantidad cero",
            PedidoValidator.validar(pedido) != null
        );

        detalle.setCantidad(2);

        comprobar(
            "Pedido válido",
            PedidoValidator.validar(pedido) == null
        );

        pedido.setProductos(List.of(detalle, detalle));

        comprobar(
            "Producto repetido",
            PedidoValidator.validar(pedido) != null
        );
    }

    private static void comprobar(String caso, boolean correcto) {
        if (!correcto) {
            throw new AssertionError("Falló: " + caso);
        }

        System.out.println("OK: " + caso);
    }
}