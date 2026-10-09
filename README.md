# ms-autenticacion

Microservicio de **identidad y seguridad** del caso BodegaNube (JVY0101). Administra los usuarios
(operarios de bodega y comercios), guarda sus contraseñas con BCrypt y emite el JWT que valida el gateway.

| | |
|---|---|
| Puerto | `8081` |
| Base de datos | PostgreSQL `bodeganube_auth` (una base por microservicio) |
| Stack | Java 17, Spring Boot 3.2, Spring Data JPA, JJWT, Spring Security Crypto (BCrypt), Maven Wrapper |

## Requerimientos que cubre
- **RF-01**: autenticación y autorización granular según el rol (`OPERARIO` o `COMERCIO`).
- **RF-06**: el usuario `COMERCIO` queda asociado a su `comercioId`, que viaja en el JWT para que cada
  comercio vea solo sus órdenes.

## Seguridad
- Las contraseñas se guardan como **hash BCrypt** (con sal y lento a propósito); nunca en texto plano y
  nunca se devuelven en la API.
- El login responde el mismo 401 si el usuario no existe o si la contraseña está mal, para no revelar qué
  usernames existen.
- El JWT se firma con HMAC usando `JWT_SECRET`, lleva los claims `rol` y `comercioId` y expira en 1 hora.

## Arquitectura en capas
```
src/main/java/com/bodeganube/autenticacion
├── controller/   AuthController, UsuarioController   reciben HTTP, validan con @Valid y delegan
├── service/      AuthService, UsuarioService         login, alta y cambios de usuario (@Transactional)
├── security/     JwtService                          firma del JWT con claims de rol y comercio
├── config/       SeguridadConfig                     bean PasswordEncoder (BCrypt)
├── repository/   UsuarioRepository                   acceso a datos con Spring Data JPA
├── model/        Usuario, Rol                        entidad JPA mapeada a la tabla usuarios
├── dto/          *Request / *Response                contrato JSON de la API, sin la contraseña
└── exception/    GlobalExceptionHandler              errores con formato JSON único (incluye 401)
```

## Modelo de datos
Tabla `usuarios` (entidad `Usuario`). Hibernate la crea al iniciar (`ddl-auto: update`).

| Columna | Tipo | Restricciones |
|---|---|---|
| id | bigint | PK, autoincremental |
| username | varchar | NOT NULL, UNIQUE |
| password | varchar | NOT NULL, hash BCrypt |
| rol | varchar | NOT NULL: `OPERARIO` o `COMERCIO` |
| comercio_id | varchar | obligatorio si el rol es `COMERCIO`; nulo para `OPERARIO` |

## Endpoints
Base: `http://localhost:8081` (o `http://localhost:8080` a través de ms-gateway).

| Método | Ruta | Qué hace | Respuestas |
|---|---|---|---|
| POST | `/api/auth/login` | Valida credenciales y devuelve el JWT | 200, 400, 401 |
| GET | `/api/auth/usuarios` | Lista usuarios | 200 |
| GET | `/api/auth/usuarios/{id}` | Obtiene un usuario | 200, 404 |
| POST | `/api/auth/usuarios` | Crea un usuario | 201 con `Location`, 400, 409 si el username existe |
| PUT | `/api/auth/usuarios/{id}` | Cambia rol, comercio y, opcionalmente, la contraseña | 200, 400, 404, 409 |
| DELETE | `/api/auth/usuarios/{id}` | Elimina un usuario | 204, 404 |

Crear un usuario y luego iniciar sesión:
```json
POST /api/auth/usuarios
{ "username": "tienda-sur", "password": "clave-segura-123", "rol": "COMERCIO", "comercioId": "comercio-123" }

POST /api/auth/login
{ "username": "tienda-sur", "password": "clave-segura-123" }
→ { "token": "eyJhbGciOiJIUzM4NCJ9...", "tokenType": "Bearer" }
```
El token se puede inspeccionar en [jwt.io](https://jwt.io) para ver los claims `rol` y `comercioId`.

Todas las respuestas de error tienen el mismo formato:
```json
{
  "timestamp": "2026-10-08T18:30:12.448",
  "status": 401,
  "error": "Unauthorized",
  "mensaje": "Usuario o contrasena incorrectos",
  "ruta": "/api/auth/login"
}
```

## Levantar el servicio desde cero

### 1. Requisitos
- JDK 17 o superior (`java -version`).
- PostgreSQL 14 o superior en `localhost:5432` (usuario y contraseña `postgres` por defecto).
- Git. **No hace falta instalar Maven**: el repositorio trae el Maven Wrapper (`mvnw` y `mvnw.cmd`).

### 2. Clonar el repositorio
```bash
git clone https://github.com/oLUCX/ms-autenticacion.git
cd ms-autenticacion
```

### 3. Crear la base de datos
Con psql:
```bash
psql -U postgres -c "CREATE DATABASE bodeganube_auth;"
```
O en pgAdmin: clic derecho en *Databases* → *Create* → *Database...* → `bodeganube_auth`.
Las tablas las crea la aplicación al iniciar.

Si no tienes PostgreSQL instalado, puedes usar Docker:
```bash
docker run -d --name bodeganube-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16
docker exec bodeganube-db psql -U postgres -c "CREATE DATABASE bodeganube_auth;"
```

### 4. Compilar, probar y empaquetar
```bash
.\mvnw.cmd clean package    # Windows (PowerShell)
./mvnw clean package        # Linux, macOS o Git Bash
```
Descarga las dependencias, compila, corre las pruebas automatizadas y genera `target/ms-autenticacion.jar`.

### 5. Ejecutar
```bash
java -jar target/ms-autenticacion.jar
```
También se puede levantar sin empaquetar con `.\mvnw.cmd spring-boot:run`. Queda escuchando en
`http://localhost:8081`.

### 6. Probar con Postman
Importa `postman/ms-autenticacion.postman_collection.json` (*Import* → archivo) y usa *Run collection*.
Ejecuta en orden 12 peticiones (crear usuario, login correcto e incorrecto, consultar, actualizar y
eliminar, más los casos de error 400, 401, 404 y 409) y cada una verifica su código HTTP.

## Configuración
Todo tiene un valor por defecto para desarrollo local y se puede cambiar con variables de entorno:

| Variable | Valor por defecto |
|---|---|
| `PORT` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/bodeganube_auth` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `JWT_SECRET` | clave de desarrollo (mínimo 32 caracteres; en un ambiente real se debe cambiar) |
| `JWT_EXPIRATION_MS` | `3600000` (1 hora) |

## Pruebas automatizadas
`.\mvnw.cmd test` corre 16 pruebas que no necesitan base de datos:
- `AuthServiceTest` y `UsuarioServiceTest` (JUnit 5 + Mockito, con BCrypt real): login, hash de la
  contraseña, username repetido y reglas del rol `COMERCIO`.
- `AuthControllerTest` y `UsuarioControllerTest` (MockMvc): 200, 201, 400, 401, 404 y que la contraseña
  nunca aparezca en la respuesta.

## Ramas
`main` tiene la versión entregada, `develop` integra el trabajo en curso y cada cambio entra desde una
rama `feature/...` con un Pull Request hacia `develop`.

## Próximos pasos
- Que ms-gateway valide la firma y la expiración de este JWT en un `GlobalFilter` (RBAC por rol).
- Proteger el CRUD de usuarios para que solo lo use un administrador.
