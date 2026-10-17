# SwisscolJava

Backend en Java del proyecto formativo Swisscol. El proyecto utiliza `HttpServer`, JDBC, MySQL y Gson para exponer servicios REST en formato JSON.

## Módulos disponibles

- Autenticación: registro e inicio de sesión.
- Productos: administración CRUD del catálogo.

## Endpoints principales

| Método | Endpoint | Función |
| --- | --- | --- |
| POST | `/api/auth/registro` | Registrar un usuario. |
| POST | `/api/auth/login` | Iniciar sesión. |
| GET | `/api/productos` | Listar productos. |
| GET | `/api/productos/{id}` | Consultar un producto. |
| POST | `/api/productos` | Registrar un producto. |
| PUT | `/api/productos/{id}` | Actualizar un producto. |
| DELETE | `/api/productos/{id}` | Eliminar un producto. |

## Requisitos

- Java 21 o superior.
- MySQL 8.
- Base de datos `database_swisscol`.
- Variable de entorno `SWISSCOL_DB_PASSWORD`.

## Ejecución

1. Verificar la estructura de la base de datos con los scripts de `database`.
2. Abrir el proyecto en Visual Studio Code.
3. Ejecutar `src/api/ServidorApi.java`.
4. Utilizar `http://localhost:8080` como URL local.

## Documentación

- `docs/API_AUTENTICACION.md`
- `docs/API_PRODUCTOS.md`
- `docs/PRUEBAS_API_AUTENTICACION.md`
- `docs/PRUEBAS_API_PRODUCTOS.md`

## Pruebas de productos

La colección `Swisscol - API de productos` ejecuta 15 solicitudes y 60 comprobaciones automáticas. Los archivos se encuentran en la carpeta `postman`.

## Pago manual por Nequi

La configuración pública se consulta en GET /api/pagos/configuracion. Devuelve metodo,
numero, titular, moneda y plazoHoras. El número y titular se definen en
SWISSCOL_NEQUI_NUMBER y SWISSCOL_NEQUI_HOLDER; si faltan o están vacíos se usan los
valores de desarrollo ya existentes, centralizados únicamente en src/config/PagoConfig.java.
Reinicia la API después de cambiar las variables. No contiene claves de acceso a Nequi.

La aprobación reserva inventario por 24 horas y genera una notificación con total
(productos + envío), cuenta de destino y plazo. El cliente reporta su referencia;
el administrador comprueba el movimiento manualmente en Nequi antes de pulsar
“Confirmar pago y pedido”. Reportar el pago no descuenta stock definitivamente.

Mis pedidos sigue mostrando el historial si falla o tarda la configuración de pago.
La campana consulta los pendientes al cargar, iniciar sesión, abrir o marcar avisos,
y cada 30 segundos mientras la página está visible. Abrir no marca automáticamente
las notificaciones como leídas.

Pruebas nuevas, tras compilar todo src con Java 21 y lib/* en el classpath:

- pruebas.PagoConfigPrueba: servidor HTTP temporal; no necesita MySQL.
- pruebas.PedidoPagoIntegracionPrueba --ejecutar: requiere MySQL y crea datos
  sintéticos con identificadores propios; los elimina en finally, sin enviar correo.
- Desde la raíz del workspace: node --test SWISSCOL/tests/flujo-pago.test.cjs.

Resultados y lista exacta de archivos: docs/VALIDACION_PAGOS.md.
