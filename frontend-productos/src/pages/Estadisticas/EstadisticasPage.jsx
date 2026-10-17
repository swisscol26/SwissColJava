import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import pedidoService from "../../services/pedidoService";

const formatoMoneda = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

function EstadisticasPage() {
  const [estadisticas, setEstadisticas] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    let activo = true;

    async function cargar() {
      setCargando(true);
      setError("");

      try {
        const respuesta =
          await pedidoService.consultarEstadisticas();

        if (activo) {
          setEstadisticas(respuesta.data);
        }
      } catch (err) {
        if (activo) {
          setEstadisticas(null);
          setError(
            err.response?.data?.mensaje ||
              "No fue posible cargar las estadísticas."
          );
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }

    cargar();

    return () => {
      activo = false;
    };
  }, [recarga]);

  return (
    <main className="container py-5">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
          <h1>Estadísticas de ventas</h1>
          <p className="text-secondary mb-0">
            Acumulado de todos los pedidos confirmados.
            No incluye solicitudes pendientes.
          </p>
        </div>

        <Link
          to="/admin/productos"
          className="btn btn-outline-secondary"
        >
          Volver a productos
        </Link>
      </div>

      <button
        type="button"
        className="btn btn-outline-primary mb-4"
        disabled={cargando}
        onClick={() => setRecarga(valor => valor + 1)}
      >
        {cargando ? "Cargando..." : "Actualizar estadísticas"}
      </button>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {cargando && (
        <p role="status">Consultando las ventas...</p>
      )}

      {!cargando && estadisticas && (
        <>
          <div className="row g-3 mb-4">
            <div className="col-md-6">
              <div className="card shadow-sm h-100">
                <div className="card-body">
                  <h2 className="h6 text-secondary">
                    Pedidos confirmados
                  </h2>
                  <p className="fs-2 fw-bold mb-0">
                    {estadisticas.pedidosConfirmados}
                  </p>
                </div>
              </div>
            </div>

            <div className="col-md-6">
              <div className="card shadow-sm h-100">
                <div className="card-body">
                  <h2 className="h6 text-secondary">
                    Total vendido
                  </h2>
                  <p className="fs-2 fw-bold text-success mb-0">
                    {formatoMoneda.format(
                      estadisticas.totalVendido
                    )}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-body">
              <h2 className="h4">Productos más vendidos</h2>
              <p className="text-secondary">
                Ordenados por unidades vendidas. Los importes
                utilizan el precio guardado en cada pedido.
              </p>

              {estadisticas.productosMasVendidos.length === 0 ? (
                <p className="mb-0">
                  Todavía no hay productos vendidos en pedidos
                  confirmados.
                </p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped align-middle">
                    <thead>
                      <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Producto</th>
                        <th scope="col" className="text-end">
                          Unidades vendidas
                        </th>
                        <th scope="col" className="text-end">
                          Total vendido
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {estadisticas.productosMasVendidos.map(
                        producto => (
                          <tr key={producto.productoId}>
                            <td>{producto.productoId}</td>
                            <td>{producto.nombre}</td>
                            <td className="text-end">
                              {producto.unidadesVendidas}
                            </td>
                            <td className="text-end">
                              {formatoMoneda.format(
                                producto.totalVendido
                              )}
                            </td>
                          </tr>
                        )
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </main>
  );
}

export default EstadisticasPage;