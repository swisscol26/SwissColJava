import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import ProductoTable from "../../components/productos/ProductoTable";
import productoService from "../../services/productoService";

/**
 * Pantalla principal del CRUD administrativo de productos.
 */
function ProductosPage() {
  const navigate = useNavigate();

  const [productos, setProductos] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  /**
   * Consulta los productos almacenados en el backend.
   */
  const cargarProductos = async () => {
    try {
      setCargando(true);
      setError("");

      const respuesta =
        await productoService.listarProductos();

      setProductos(respuesta.data);
    } catch (err) {
      console.error("Error al cargar productos:", err);

      setError(
        "No fue posible cargar los productos. " +
          "Comprueba que la API Java esté ejecutándose."
      );
    } finally {
      setCargando(false);
    }
  };

  /**
   * useEffect ejecuta la consulta al abrir la pantalla.
   */
  useEffect(() => {
    cargarProductos();
  }, []);

  /**
   * Solicita confirmación y elimina el producto.
   */
  const eliminarProducto = async (producto) => {
    const confirmado = window.confirm(
      `¿Está seguro de eliminar el producto "${producto.name}"?`
    );

    if (!confirmado) {
      return;
    }

    try {
      setError("");
      setMensaje("");

      await productoService.eliminarProducto(
        producto.productId
      );

      setMensaje("Producto eliminado correctamente.");

      await cargarProductos();
    } catch (err) {
      console.error("Error al eliminar producto:", err);

      setError(
        err.response?.data?.mensaje ||
          "No fue posible eliminar el producto."
      );
    }
  };

  /**
   * Filtra la tabla por ID o nombre.
   */
  const productosFiltrados = productos.filter(
    (producto) => {
      const texto = busqueda.toLowerCase().trim();

      return (
        producto.name.toLowerCase().includes(texto) ||
        String(producto.productId).includes(texto)
      );
    }
  );

  return (
    <main className="container py-5">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h1 className="mb-1">
            Gestión de productos
          </h1>

          <p className="text-secondary mb-0">
            Módulo administrativo de Swisscol
          </p>
        </div>

        <button
          type="button"
          className="btn btn-danger"
          onClick={() =>
            navigate("/admin/productos/nuevo")
          }
        >
          Registrar producto
        </button>
      </div>

      {mensaje && (
        <div className="alert alert-success">
          {mensaje}
        </div>
      )}

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      <div className="card shadow-sm">
        <div className="card-body">
          <div className="mb-4">
            <label
              htmlFor="busqueda"
              className="form-label"
            >
              Buscar producto
            </label>

            <input
              id="busqueda"
              type="search"
              className="form-control"
              placeholder="Buscar por ID o nombre..."
              value={busqueda}
              onChange={(evento) =>
                setBusqueda(evento.target.value)
              }
            />
          </div>

          {cargando ? (
            <div className="text-center py-5">
              <div
                className="spinner-border text-danger"
                role="status"
              >
                <span className="visually-hidden">
                  Cargando productos...
                </span>
              </div>

              <p className="mt-3">
                Cargando productos...
              </p>
            </div>
          ) : (
            <ProductoTable
              productos={productosFiltrados}
              onVer={(productId) =>
                navigate(
                  `/admin/productos/${productId}`
                )
              }
              onEditar={(productId) =>
                navigate(
                  `/admin/productos/${productId}/editar`
                )
              }
              onEliminar={eliminarProducto}
            />
          )}
        </div>
      </div>
    </main>
  );
}

export default ProductosPage;