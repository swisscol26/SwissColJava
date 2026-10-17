# Pruebas de la API de productos - Swisscol

## Archivos de Postman

- Colección: `postman/Swisscol-Productos.postman_collection.json`
- Ambiente: `postman/Swisscol Productos Local.postman_environment.json`

## Resultado comprobado

La colección fue ejecutada contra la API local conectada con MySQL.

| Indicador | Resultado |
| --- | ---: |
| Solicitudes ejecutadas | 15 |
| Comprobaciones automáticas | 60 |
| Solicitudes fallidas | 0 |
| Comprobaciones fallidas | 0 |
| Errores del Runner | 0 |

El resultado completo se conserva en:

```text
docs/evidencias/EV04/resultados/newman-productos-resultados.json
```

## Casos exitosos

| Solicitud | Método | Código esperado |
| --- | --- | ---: |
| Listar productos | GET | 200 |
| Consultar producto existente | GET | 200 |
| Registrar producto temporal | POST | 201 |
| Actualizar producto temporal | PUT | 200 |
| Consultar producto actualizado | GET | 200 |
| Eliminar producto temporal | DELETE | 200 |

## Casos de error

| Solicitud | Situación | Código esperado |
| --- | --- | ---: |
| Consultar con ID no numérico | `abc` | 400 |
| Consultar producto inexistente | ID no registrado | 404 |
| Registrar ID duplicado | ID 12 | 409 |
| Registrar precio negativo | `price = -1` | 400 |
| Registrar categoría inexistente | categoría 999999999 | 400 |
| Actualizar producto inexistente | ID no registrado | 404 |
| Eliminar producto inexistente | producto ya eliminado | 404 |
| Utilizar método no permitido | PATCH | 405 |
| Enviar tipo de contenido incorrecto | `text/plain` | 415 |

## Comprobaciones por solicitud

Cada solicitud contiene cuatro pruebas automáticas:

1. Código de estado HTTP.
2. Resultado funcional exitoso o rechazado.
3. Datos o mensaje JSON esperado.
4. Encabezado `Content-Type` en formato JSON.

## Ejecución en Postman

1. Ejecutar `src/api/ServidorApi.java`.
2. Importar el ambiente `Swisscol Productos Local`.
3. Importar la colección `Swisscol - API de productos`.
4. Seleccionar el ambiente importado.
5. Abrir el Collection Runner.
6. Ejecutar la colección completa en el orden guardado.
7. Comprobar que aparezcan 60 pruebas aprobadas y 0 fallidas.

El producto temporal se crea, actualiza, consulta y elimina dentro de la misma ejecución. Por esta razón las pruebas no alteran permanentemente el catálogo.
