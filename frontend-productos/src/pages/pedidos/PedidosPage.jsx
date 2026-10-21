import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import "./PedidosPage.css";
import pedidoService from "../../services/pedidoService";

function PedidosPage() {
    const [pedidos, setPedidos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [procesando, setProcesando] = useState(null);
    const [error, setError] = useState("");
    const [mensaje, setMensaje] = useState("");
    const [costosEnvio, setCostosEnvio] = useState({});
    const [guardandoEnvio, setGuardandoEnvio] = useState(null);

    const confirmacionEnCurso = useRef(false);

    const cargarPedidos = useCallback(async () => {
        setCargando(true);
        setError("");

        try {
            const respuesta = await pedidoService.listarPendientes();
            setPedidos(respuesta.data);
            setCostosEnvio(
    Object.fromEntries(
        respuesta.data.map(pedido => [
            pedido.pedidoId,
            pedido.costoEnvio == null
                ? ""
                : String(pedido.costoEnvio)
        ])
    )
);
        } catch (fallo) {
            setError(
                fallo.response?.data?.mensaje
                || "No fue posible cargar los pedidos."
            );
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        const temporizador = window.setTimeout(() => {
            cargarPedidos();
        }, 0);

        return () => window.clearTimeout(temporizador);
    }, [cargarPedidos]);

    async function confirmarPedido(pedidoId) {
        if (confirmacionEnCurso.current) return;

        const aceptado = window.confirm(
            `¿Confirmar el pedido #${pedidoId}? ` +
            "Confirma solo si ya verificaste manualmente en Nequi el movimiento, " +
            "la referencia y el total con envío. Se descontarán sus unidades del inventario."
        );

        if (!aceptado) return;

        confirmacionEnCurso.current = true;
        setProcesando(pedidoId);
        setError("");
        setMensaje("");

        try {
            await pedidoService.confirmarPedido(pedidoId);

            setPedidos(actuales =>
                actuales.filter(pedido => pedido.pedidoId !== pedidoId)
            );

            setMensaje(
                `Pedido #${pedidoId} confirmado. Inventario actualizado.`
            );
        } catch (fallo) {
            setError(
                fallo.response?.data?.mensaje
                || "No se pudo comprobar el resultado. " +
                   "Actualiza la lista antes de volver a intentarlo."
            );
        } finally {
            confirmacionEnCurso.current = false;
            setProcesando(null);
        }
    }

    async function cancelarSolicitud(pedidoId) {
        if (confirmacionEnCurso.current) return;

        const aceptado = window.confirm(
            `¿Cancelar la solicitud #${pedidoId}? ` +
            "Se retirará de pendientes y se liberarán sus reservas. El historial se conservará. " +
            "Si reportaron un pago, verifica primero que sea una solicitud de prueba o resuelve el pago con el cliente. Esta acción no realiza reembolsos."
        );

        if (!aceptado) return;

        confirmacionEnCurso.current = true;
        setProcesando(pedidoId);
        setError("");
        setMensaje("");

        try {
            await pedidoService.cancelarSolicitud(pedidoId);

            setPedidos(actuales =>
                actuales.filter(pedido => pedido.pedidoId !== pedidoId)
            );

            setMensaje(
                `Solicitud #${pedidoId} cancelada. Reservas liberadas.`
            );
        } catch (fallo) {
            setError(
                fallo.response?.data?.mensaje
                || "No se pudo comprobar el resultado. " +
                   "Actualiza la lista antes de volver a intentarlo."
            );
        } finally {
            confirmacionEnCurso.current = false;
            setProcesando(null);
        }
    }

    async function aprobarParaPago(pedidoId) {
    if (confirmacionEnCurso.current) return;

    const pedido = pedidos.find(
        item => item.pedidoId === pedidoId
    );

    if (pedido?.costoEnvio == null) {
        setError(
            "Guarda el costo de envío antes de aprobar el pedido."
        );
        return;
    }

    const aceptado = window.confirm(
        `¿Aprobar el pedido #${pedidoId} para pago?\n\n` +
        "Las unidades quedarán reservadas durante 24 horas."
    );

    if (!aceptado) return;

    confirmacionEnCurso.current = true;
    setProcesando(pedidoId);
    setError("");
    setMensaje("");

    try {
        await pedidoService.aprobarParaPago(pedidoId);

        setPedidos(actuales =>
            actuales.map(pedido =>
                pedido.pedidoId === pedidoId
                    ? {
                        ...pedido,
                        estado: "PENDIENTE_PAGO"
                    }
                    : pedido
            )
        );

        setMensaje(
            `Pedido #${pedidoId} aprobado. ` +
            "Las unidades se reservaron durante 24 horas."
        );
    } catch (fallo) {
        setError(
            fallo.response?.data?.mensaje ||
            "No fue posible aprobar el pedido para pago."
        );
    } finally {
        confirmacionEnCurso.current = false;
        setProcesando(null);
    }
}

    async function guardarCostoEnvio(event, pedidoId) {
    event.preventDefault();

    if (confirmacionEnCurso.current) return;

    setError("");
    setMensaje("");

    const texto = String(costosEnvio[pedidoId] ?? "").trim();
    const costo = Number(texto);

    if (
        texto === "" ||
        !Number.isFinite(costo) ||
        costo < 0 ||
        costo > 99999999.99 ||
        !/^\d+(\.\d{1,2})?$/.test(texto)
    ) {
        setError(
            "Indica un costo válido, con máximo dos decimales. " +
            "Usa 0 si el envío es gratis."
        );
        return;
    }

    confirmacionEnCurso.current = true;
    setGuardandoEnvio(pedidoId);

    try {
        await pedidoService.actualizarCostoEnvio(pedidoId, costo);

        setPedidos(actuales =>
            actuales.map(pedido =>
                pedido.pedidoId === pedidoId
                    ? { ...pedido, costoEnvio: costo }
                    : pedido
            )
        );

        setMensaje(
            `Costo de envío del pedido #${pedidoId} guardado.`
        );
    } catch (fallo) {
        setError(
            fallo.response?.data?.mensaje ||
            "No se pudo comprobar el guardado. " +
            "Actualiza la lista para consultar el costo registrado."
        );
    } finally {
        confirmacionEnCurso.current = false;
        setGuardandoEnvio(null);
    }
}

    function formatoPrecio(valor) {
        return Number(valor).toLocaleString("es-CO", {
            style: "currency",
            currency: "COP",
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        });
    }

    function formatoFechaHora(texto) {
        if (!texto) return "Sin fecha";
        const fecha = new Date(/(?:Z|[+-]\d{2}:\d{2})$/.test(texto) ? texto : texto + "-05:00");
        if (Number.isNaN(fecha.getTime())) return "Fecha no válida";
        return new Intl.DateTimeFormat("es-CO", {
            dateStyle: "medium", timeStyle: "short", timeZone: "America/Bogota"
        }).format(fecha) + " (Colombia)";
    }

    return (
        <main className="container py-4 pedidos-page">
            <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                <h1>Pedidos pendientes</h1>

                <Link
                    to="/admin/productos"
                    className="btn btn-outline-secondary"
                >
                    Volver a productos
                </Link>
            </div>

            <p>
                Revisa cada solicitud antes de confirmar la venta.
                La confirmación descuenta las existencias.
            </p>

            <button
                className="btn btn-outline-primary mb-3"
                onClick={cargarPedidos}
                disabled={cargando || procesando !== null}
            >
                Actualizar lista
            </button>

            {mensaje && (
                <div className="alert alert-success" role="status">
                    {mensaje}
                </div>
            )}

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {cargando && <p role="status">Cargando pedidos...</p>}

            {!cargando && !error && pedidos.length === 0 && (
                <p>No hay pedidos pendientes.</p>
            )}

            {!cargando && pedidos.map(pedido => (
                <article className="card mb-4 pedido-card" key={pedido.pedidoId}>
                    <div className="card-body">
                        <div className="pedido-heading">
                            <h2 className="h4">Pedido #{pedido.pedidoId}</h2>
                            <span className={`pedido-estado pedido-estado--${pedido.estado.toLowerCase()}`}>
                                {({ PENDIENTE: "Pendiente de revisión", PENDIENTE_PAGO: "Esperando pago", PAGO_POR_VERIFICAR: "Pago por verificar" })[pedido.estado] || pedido.estado}
                            </span>
                        </div>
                        <div className="pedido-cliente">

                        <p className="mb-1">
                            <strong>Cliente:</strong> {pedido.nombreCliente}
                        </p>

                        <p className="mb-1">
                            <strong>Correo:</strong> {pedido.correoCliente}
                        </p>

                        <p className="mb-1">
                            <strong>Fecha:</strong> {pedido.fecha}
                        </p>

                        </div>

                        <div className="pedido-envio">
    <h3 className="h5 mb-3">Datos de envío</h3>

    {pedido.direccion ? (
        <>
            <p className="mb-1">
                <strong>Destinatario:</strong>{" "}
                {pedido.nombreDestinatario || "No registrado"}
            </p>

            <p className="mb-1">
                <strong>Teléfono:</strong>{" "}
                {pedido.telefonoDestinatario || "No registrado"}
            </p>

            <p className="mb-1">
                <strong>Departamento:</strong>{" "}
                {pedido.departamento || "No registrado"}
            </p>

            <p className="mb-1">
                <strong>Ciudad o municipio:</strong>{" "}
                {pedido.ciudad || "No registrado"}
            </p>

            <p className="mb-1 text-break">
                <strong>Dirección:</strong> {pedido.direccion}
            </p>

            {pedido.barrio && (
                <p className="mb-1 text-break">
                    <strong>Barrio:</strong> {pedido.barrio}
                </p>
            )}

            {pedido.indicaciones && (
                <p
                    className="mb-0 text-break"
                    style={{ whiteSpace: "pre-wrap" }}
                >
                    <strong>Indicaciones:</strong>{" "}
                    {pedido.indicaciones}
                </p>
            )}
        </>
    ) : (
        <p className="text-muted mb-0">
            Este pedido no tiene datos de envío registrados.
        </p>
    )}
</div>

                        <ul className="list-group mb-3 pedido-productos">
                            {pedido.productos.map(producto => (
                                <li
                                    className="list-group-item"
                                    key={producto.productoId}
                                >
                                    <strong>{producto.nombre}</strong>
                                    <span>Cantidad: {producto.cantidad}</span>
                                    <span>Precio unitario: {formatoPrecio(producto.precioUnitario)}</span>
                                </li>
                            ))}
                        </ul>

                                            <p className="fw-bold">
                        Total de productos: {formatoPrecio(pedido.total)}
                    </p>

                    <form
                        className="pedido-costo"
                        onSubmit={event =>
                            guardarCostoEnvio(event, pedido.pedidoId)
                        }
                    >
                        <label
                            className="form-label"
                            htmlFor={`costo-envio-${pedido.pedidoId}`}
                        >
                            Costo de envío (COP)
                        </label>

                        <div className="d-flex flex-wrap gap-2">
                            <input
                                type="number"
                                className="form-control"

                                id={`costo-envio-${pedido.pedidoId}`}
                                aria-describedby={`ayuda-envio-${pedido.pedidoId}`}
                                min="0"
                                max="99999999.99"
                                step="0.01"
                                required
                                placeholder="Pendiente de definir"
                                value={costosEnvio[pedido.pedidoId] ?? ""}
                                onChange={event =>
                                    setCostosEnvio(actuales => ({
                                        ...actuales,
                                        [pedido.pedidoId]: event.target.value
                                    }))
                                }
                                disabled={
                                    pedido.estado !== "PENDIENTE" ||
                                    cargando ||
                                    procesando !== null ||
                                    guardandoEnvio !== null
                                }
                            />

                                <button
                                    type="submit"
                                    className="btn btn-outline-primary"
                                    disabled={
                                        pedido.estado !== "PENDIENTE" ||
                                        procesando !== null ||
                                        guardandoEnvio !== null
                                    }
                                >
                                    {guardandoEnvio === pedido.pedidoId
                                        ? "Guardando..."
                                        : "Guardar envío"}
                                </button>
                            </div>

                            <p
                                className="form-text"
                                id={`ayuda-envio-${pedido.pedidoId}`}
                            >
                                {pedido.estado === "PENDIENTE"
                                    ? "Usa 0 para envío gratis. Guarda cualquier cambio para actualizar el total."
                                    : "El costo de envío queda fijado al aprobar el pedido para pago."}
                            </p>

                            <p className="mb-1">
                                <strong>Envío guardado:</strong>{" "}
                                {pedido.costoEnvio == null
                                    ? "Pendiente de definir"
                                    : formatoPrecio(pedido.costoEnvio)}
                            </p>

                            <p className="fw-bold mb-0">
                                Total con envío:{" "}
                                {pedido.costoEnvio == null
                                    ? "Pendiente de definir"
                                    : formatoPrecio(
                                        Number(pedido.total) +
                                        Number(pedido.costoEnvio)
                                    )}
                            </p>
                        </form>

                        <div className="pedido-acciones">
                        <button
                            className="btn btn-outline-danger"
                            disabled={procesando !== null || guardandoEnvio !== null}
                            onClick={() => cancelarSolicitud(pedido.pedidoId)}
                        >
                            Cancelar solicitud
                        </button>

                        {pedido.estado === "PENDIENTE" && (
                            <button
                                className="btn btn-primary"
                                disabled={
                                    procesando !== null ||
                                    guardandoEnvio !== null ||
                                    pedido.costoEnvio == null
                                }
                                onClick={() =>
                                    aprobarParaPago(pedido.pedidoId)
                                }
                            >
                                {procesando === pedido.pedidoId
                                    ? "Aprobando..."
                                    : "Aprobar para pago"}
                            </button>
                        )}

                        {pedido.estado === "PENDIENTE_PAGO" && (
                            <p className="alert alert-info mb-0">
                                Pedido aprobado. Esperando que el cliente realice
                                el pago por Nequi.
                            </p>
                        )}

                        {pedido.estado === "PAGO_POR_VERIFICAR" && (
                            <div className="d-grid gap-3">
                                <div className="alert alert-warning mb-0">
                                    <p className="fw-bold mb-2">
                                        Pago reportado por el cliente
                                    </p>
                                    <p>Verifica manualmente el movimiento en Nequi antes de confirmar el pago y pedido.</p>
                                    <p className="mb-1 text-break">
                                        <strong>Referencia:</strong>{" "}
                                        {pedido.referenciaPago || "Sin referencia"}
                                    </p>
                                    <p className="mb-1">
                                        <strong>Reportado:</strong>{" "}
                                        {formatoFechaHora(pedido.pagoReportadoEn)}
                                    </p>
                                    <p className="mb-0">
                                        <strong>Total a verificar:</strong>{" "}
                                        {formatoPrecio(
                                            Number(pedido.total) +
                                            Number(pedido.costoEnvio || 0)
                                        )}
                                    </p>
                                </div>

                                <button
                                    className="btn btn-success"
                                    disabled={
                                        procesando !== null ||
                                        guardandoEnvio !== null
                                    }
                                    onClick={() =>
                                        confirmarPedido(pedido.pedidoId)
                                    }
                                >
                                    {procesando === pedido.pedidoId
                                        ? "Confirmando..."
                                        : "Confirmar pago y pedido"}
                                </button>
                            </div>
                        )}
                        </div>
                    </div>
                </article>
            ))}
        </main>
    );
}

export default PedidosPage;
