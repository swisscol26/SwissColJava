# API REST de productos - Swisscol

## Propósito

La API administra el catálogo de productos del proyecto Swisscol. Fue desarrollada en Java con `HttpServer`, utiliza MySQL mediante JDBC y entrega todas sus respuestas en formato JSON.

URL base local:

```text
http://localhost:8080/api/productos
```

## Organización del código

| Paquete | Responsabilidad |
| --- | --- |
| `modelo` | Representa los datos del producto. |
| `dto` | Define el resultado de cada operación. |
| `dao` | Ejecuta consultas parametrizadas en MySQL. |
| `middlewares` | Valida los datos recibidos. |
| `services` | Aplica reglas del negocio y normaliza información. |
| `controllers` | Procesa HTTP, JSON, CORS y códigos de respuesta. |
| `routes` | Registra la ruta `/api/productos`. |
| `api` | Inicia el servidor en el puerto 8080. |

## Modelo JSON

```json
{
  "productId": 101,
  "name": "Victorinox Spartan",
  "description": "Navaja suiza multipropósito",
  "price": 189900,
  "stock": 25,
  "image": "spartan.jpg",
  "categoryId": 1
}
```

## Endpoints

### Listar productos

```http
GET /api/productos
```

- `200 OK`: devuelve un arreglo JSON con los productos.
- `500 Internal Server Error`: falla de conexión o consulta.

### Consultar un producto

```http
GET /api/productos/{id}
```

- `200 OK`: devuelve el producto encontrado.
- `400 Bad Request`: el identificador no es un entero positivo.
- `404 Not Found`: el producto no existe.
- `500 Internal Server Error`: falla de conexión o consulta.

### Registrar un producto

```http
POST /api/productos
Content-Type: application/json
```

Ejemplo:

```json
{
  "productId": 101,
  "name": "Victorinox Spartan",
  "description": "Navaja suiza multipropósito",
  "price": 189900,
  "stock": 25,
  "image": "spartan.jpg",
  "categoryId": 1
}
```

- `201 Created`: producto registrado.
- `400 Bad Request`: datos inválidos o categoría inexistente.
- `409 Conflict`: identificador duplicado.
- `415 Unsupported Media Type`: el cuerpo no fue enviado como JSON.
- `500 Internal Server Error`: falla no controlada de persistencia.

### Actualizar un producto

```http
PUT /api/productos/{id}
Content-Type: application/json
```

El identificador utilizado es el de la URL. El cuerpo contiene nombre, descripción, precio, stock, imagen y categoría.

- `200 OK`: producto actualizado.
- `400 Bad Request`: identificador o datos inválidos.
- `404 Not Found`: producto inexistente.
- `409 Conflict`: conflicto de integridad.
- `415 Unsupported Media Type`: el cuerpo no fue enviado como JSON.
- `500 Internal Server Error`: falla no controlada de persistencia.

### Eliminar un producto

```http
DELETE /api/productos/{id}
```

- `200 OK`: producto eliminado.
- `400 Bad Request`: identificador inválido.
- `404 Not Found`: producto inexistente.
- `409 Conflict`: el producto está relacionado con compras o carritos.
- `500 Internal Server Error`: falla no controlada de persistencia.

### Método no permitido

Cualquier método distinto de `GET`, `POST`, `PUT`, `DELETE` u `OPTIONS` recibe:

- `405 Method Not Allowed`.

## Validaciones

- ID entero mayor que cero durante el registro.
- Nombre obligatorio entre 2 y 45 caracteres.
- Descripción opcional de máximo 1000 caracteres.
- Precio mayor que cero.
- Stock igual o mayor que cero.
- Imagen opcional de máximo 45 caracteres.
- Categoría existente en la tabla `category`.
- `Content-Type: application/json` en `POST` y `PUT`.

## Seguridad y manejo de errores

- Todas las consultas utilizan `PreparedStatement`.
- Las excepciones SQL se convierten en respuestas controladas.
- Los errores de integridad se responden con `409` cuando corresponde.
- La API no entrega mensajes internos de MySQL al cliente.
- CORS se limita a los orígenes locales utilizados durante el desarrollo.

## Ejecución

1. Iniciar MySQL y verificar `database_swisscol`.
2. Configurar la variable `SWISSCOL_DB_PASSWORD`.
3. Ejecutar `src/api/ServidorApi.java`.
4. Confirmar en la consola la dirección `http://localhost:8080`.
5. Importar en Postman los archivos incluidos en la carpeta `postman`.
