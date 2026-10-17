package middlewares;

import dto.DetallePedidoSolicitud;
import dto.PedidoSolicitud;

import java.util.HashSet;
import java.util.Set;

public class PedidoValidator {

    public static String validar(PedidoSolicitud solicitud) {

        if (solicitud == null
                || solicitud.getProductos() == null
                || solicitud.getProductos().isEmpty()) {
            return "El pedido debe contener al menos un producto.";
        }

        Set<Integer> productosRevisados = new HashSet<>();

        for (DetallePedidoSolicitud detalle : solicitud.getProductos()) {

            if (detalle == null) {
                return "El pedido contiene un detalle vacío.";
            }

            if (detalle.getProductoId() <= 0) {
                return "El identificador del producto debe ser mayor que cero.";
            }

            if (detalle.getCantidad() <= 0) {
                return "La cantidad debe ser mayor que cero.";
            }

            if (!productosRevisados.add(detalle.getProductoId())) {
                return "Un producto no puede aparecer repetido en el pedido.";
            }
        }

        return null;
    }
}