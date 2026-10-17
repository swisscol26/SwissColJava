package services;

import dao.PedidoDAO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ReservaService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private final NotificacionService notificacionService =
        new NotificacionService();

    private final ScheduledExecutorService ejecutor =
        Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(
                tarea,
                "swisscol-reservas"
            );

            hilo.setDaemon(true);
            return hilo;
        });

    private boolean iniciado = false;

    public synchronized void iniciar() {
        if (iniciado) {
            return;
        }

        ejecutor.scheduleWithFixedDelay(
            this::revisarReservas,
            0,
            1,
            TimeUnit.MINUTES
        );

        iniciado = true;
    }

    private void revisarReservas() {
        try {
            for (int pedidoId : pedidoDAO.listarReservasVencidas()) {
                try {
                    if (pedidoDAO.liberarReservaVencida(pedidoId)) {
                        System.out.println(
                            "Reserva vencida liberada: pedido #"
                                + pedidoId
                        );

                        try {
                            notificacionService.crearParaPedido(
                                pedidoId,
                                "La reserva de tu pedido #" + pedidoId
                                    + " venció porque no se reportó el pago dentro de las 24 horas.",
                                "VENCIDO"
                            );
                        } catch (Exception falloNotificacion) {
                            System.err.println(
                                "Reserva del pedido #" + pedidoId
                                    + " liberada, pero no se pudo crear la notificación. Tipo: "
                                    + falloNotificacion.getClass().getSimpleName()
                            );
                        }
                    }
                } catch (Exception e) {
                    System.err.println(
                        "No se pudo liberar la reserva del pedido #"
                            + pedidoId
                            + ". Tipo: "
                            + e.getClass().getSimpleName()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println(
                "No se pudieron consultar las reservas vencidas. Tipo: "
                    + e.getClass().getSimpleName()
            );
        }
    }

    public synchronized void detener() {
        ejecutor.shutdown();
    }
}
