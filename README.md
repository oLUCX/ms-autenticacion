# ms-autenticacion

Microservicio de **Gestión de Identidades y Seguridad** del caso BodegaNube (asignatura JVY0101).

## Responsabilidad (bounded context)
Autenticar usuarios, validar credenciales y emitir JSON Web Tokens (JWT) con claims de rol
(`OPERARIO` / `COMERCIO`) y `comercioId`. Ver sección 4.1 del informe de arquitectura.

## Requerimientos que cubre
- **RF-01**: autenticación y autorización granular según rol de usuario.

## Stack
Java 17, Spring Boot 3.2, Spring Data JPA, PostgreSQL, JJWT.

## Endpoints
| Método | Endpoint          | Descripción                                   |
|--------|-------------------|------------------------------------------------|
| POST   | `/api/auth/login` | Valida credenciales y devuelve un JWT firmado. |

## Cómo correrlo localmente
1. Crear una base PostgreSQL llamada `bodeganube_auth`.
2. Ajustar `src/main/resources/application.yml` si tu usuario/clave de PostgreSQL son distintos.
3. (Opcional) definir la variable de entorno `JWT_SECRET` con una clave real de 32+ caracteres.
4. `mvn spring-boot:run`

## Próximos pasos (fuera del alcance de este esqueleto)
- Hashear las contraseñas con `BCryptPasswordEncoder` en vez de comparación en texto plano.
- Endpoint de registro de usuarios y asignación de comercioId.
- Filtro de validación de JWT en `ms-gateway`.
