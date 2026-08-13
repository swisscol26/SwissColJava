import { useEffect, useState } from "react";
import {
  useNavigate,
  useParams,
} from "react-router-dom";

import productoService from "../../services/productoService";

/**
 * Pantalla de consulta individual de un producto.
 */
function ProductoDetallePage() {
  const navigate = useNavigate();
  const { productId } = useParams();

  const [producto, setProducto] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const cargarProducto = async () => {
      try {
        setCargando(true);
        setError("");

        const respuesta =
          await productoService.buscarProductoPorId(
            productId
          );

        setProducto(respuesta.data);
      } catch (err) {
        console.error(
          "Error al consultar producto:",
          err
        );

        setError(
          err.response?.data?.mensaje ||
            "No fue posible consultar el producto."
        );
      } finally {
        setCargando(false);
      }
    };

    cargarProducto();
  }, [productId]);

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="mb-1">
            Detalle del producto
          </h1>
          <p className="text-secondary mb-0">
            Información almacenada en el sistema.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() =>
            navigate("/admin/productos")
          }
        >
          Volver
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      {cargando && (
        <div className="text-center py-5">
          <div
            className="spinner-border text-danger"
            role="status"
          >
            <span className="visually-hidden">
              Cargando producto...
            </span>
          </div>
        </div>
      )}

      {!cargando && producto && (
        <div className="card shadow-sm">
          <div className="card-header bg-dark text-white">
            <h2 className="h5 mb-0">
              {producto.name}
            </h2>
          </div>

          <div className="card-body p-4">
            <div className="row g-4">
              <div className="col-md-4">
                <strong>ID:</strong>
                <p>{producto.productId}</p>
              </div>

              <div className="col-md-4">
                <strong>Precio:</strong>
                <p>
                  $
                  {Number(
                    producto.price
                  ).toLocaleString("es-CO")}
                </p>
              </div>

              <div className="col-md-4">
                <strong>Stock:</strong>
                <p>{producto.stock}</p>
              </div>

              <div className="col-md-4">
                <strong>Categoría:</strong>
                <p>{producto.categoryId}</p>
              </div>

              <div className="col-md-8">
                <strong>Imagen:</strong>
                <p>
                  {producto.image ||
                    "Sin imagen registrada"}
                </p>
              </div>

              <div className="col-12">
                <strong>Descripción:</strong>
                <p className="mb-0">
                  {producto.description ||
                    "Sin descripción"}
                </p>
              </div>
            </div>

            <div className="d-flex justify-content-end mt-4">
              <button
                type="button"
                className="btn btn-primary"
                onClick={() =>
                  navigate(
                    `/admin/productos/${productId}/editar`
                  )
                }
              >
                Editar producto
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default ProductoDetallePage;