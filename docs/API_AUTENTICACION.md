# API de autenticación — Swisscol

Servicio web desarrollado en Java para registrar usuarios y validar
el inicio de sesión utilizando MySQL.

## URL base

http://localhost:8080/api/auth

## Endpoints

### Registrar usuario

POST /api/auth/registro

Cuerpo JSON:

{
  "nombre": "Usuario Evidencia",
  "correo": "usuario@swisscol.com",
  "password": "ClaveSegura123",
  "confirmarPassword": "ClaveSegura123"
}

Respuesta exitosa:

- Código HTTP: 201 Created
- Mensaje: Usuario registrado correctamente.

Posibles errores:

- 400: datos inválidos o contraseñas diferentes.
- 409: el correo ya está registrado.
- 500: error interno del servidor.

### Iniciar sesión

POST /api/auth/login

Cuerpo JSON:

{
  "correo": "usuario@swisscol.com",
  "password": "ClaveSegura123"
}

Respuesta exitosa:

- Código HTTP: 200 OK
- Mensaje: Inicio de sesión exitoso.

Posibles errores:

- 400: datos incompletos o correo inválido.
- 401: correo o contraseña incorrectos.
- 500: error interno del servidor.

## Seguridad

- Las consultas utilizan PreparedStatement para reducir el riesgo de
  inyección SQL.
- Las contraseñas se almacenan mediante PBKDF2WithHmacSHA256.
- Cada contraseña utiliza una sal aleatoria.
- El correo electrónico tiene una restricción UNIQUE en MySQL.
- El registro público asigna siempre el rol CUSTOMER.
- El login no revela cuál de las credenciales es incorrecta.

## Ejecución

1. Iniciar MySQL.
2. Configurar la variable SWISSCOL_DB_PASSWORD.
3. Ejecutar src/api/ServidorApi.java.
4. Probar los endpoints desde Postman.

## Casos comprobados

- Registro exitoso: 201.
- Correo duplicado: 409.
- Login exitoso: 200.
- Contraseña incorrecta: 401.
- Contraseñas de registro diferentes: 400.