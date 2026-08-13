// Tabla reutilizable del módulo administrativo de productos.
function ProductoTable({
  productos,
  onVer,
  onEditar,
  onEliminar,
}) {
  if (productos.length === 0) {
    return (
      <div className="alert alert-info text-center">
        No hay productos registrados.
      </div>
    );
  }

  return (
    <div className="table-responsive">
      <table className="table table-striped table-hover align-middle">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Categoría</th>
            <th className="text-center">Acciones</th>
          </tr>
        </thead>

        <tbody>
          {productos.map((producto) => (
            <tr key={producto.productId}>
              <td>{producto.productId}</td>
              <td>{producto.name}</td>

              <td>
                ${Number(producto.price).toLocaleString("es-CO")}
              </td>

              <td>{producto.stock}</td>
              <td>{producto.categoryId}</td>

              <td className="text-center">
                <div className="d-flex gap-2 justify-content-center">
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={() => onVer(producto.productId)}
                  >
                    Ver
                  </button>

                  <button
                    type="button"
                    className="btn btn-outline-primary btn-sm"
                    onClick={() => onEditar(producto.productId)}
                  >
                    Editar
                  </button>

                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => onEliminar(producto)}
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ProductoTable;