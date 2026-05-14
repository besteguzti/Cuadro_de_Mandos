# Cuadro de Mandos

Cuadro de mandos para visualizar el estado de los puntos de acceso Aruba y mantener una copia actualizada en MySQL.

## Arquitectura

- `dashboard`: backend Spring Boot.
- `frontend`: interfaz React + Vite.
- `dashboard.access_points`: tabla MySQL con una fila por AP, identificada por numero de serie.
- Aruba Central: fuente de datos de APs y firmware.

## Requisitos

- Java 17.
- Maven.
- Node.js y npm.
- MySQL con una base de datos llamada `dashboard`.
- Token OAuth de Aruba guardado en la tabla `oauth_tokens`.

## Variables de Entorno

El backend usa estas variables:

```powershell
$env:DB_USERNAME="usuario_mysql"
$env:DB_PASSWORD="password_mysql"
$env:ARUBA_CLIENT_ID="client_id_aruba"
$env:ARUBA_CLIENT_SECRET="client_secret_aruba"
```

La URL base de Aruba esta configurada en `dashboard/src/main/resources/application.properties`:

```properties
aruba.base.url=https://apigw-eucentral3.central.arubanetworks.com
aruba.sync.initial-delay-ms=60000
aruba.sync.fixed-rate-ms=3600000
```

## Arrancar Backend

Desde la carpeta `dashboard`:

```powershell
mvn spring-boot:run
```

El backend queda disponible en:

```text
http://localhost:8080
```

## Arrancar Frontend

Desde la carpeta `frontend`:

```powershell
npm install
npm run dev
```

El frontend queda disponible en:

```text
http://localhost:5173
```

## Endpoints Principales

Resumen del dashboard. Consulta Aruba, sincroniza APs en MySQL y devuelve KPIs:

```http
GET http://localhost:8080/aruba/summary
```

Listado directo desde Aruba:

```http
GET http://localhost:8080/aruba/aps
```

Listado guardado en MySQL:

```http
GET http://localhost:8080/aruba/stored-aps
```

Sincronizacion manual de APs en MySQL:

```http
POST http://localhost:8080/aruba/sync-aps
```

## Sincronizacion Automatica

El backend sincroniza APs automaticamente con `ArubaScheduler`:

- Primera ejecucion: configurable con `aruba.sync.initial-delay-ms`.
- Frecuencia: configurable con `aruba.sync.fixed-rate-ms`.

Ademas, `GET /aruba/summary` tambien sincroniza los APs usando la misma lista que obtiene para calcular los KPIs.

Cada AP se guarda en `access_points` usando el numero de serie como clave logica. `firstSeenAt` guarda cuando se vio por primera vez y no se sobrescribe; `lastSeenAt` se actualiza en cada sincronizacion.

## Comprobar Flujo Real con MySQL

1. Arranca MySQL y comprueba que existe la base de datos:

```sql
CREATE DATABASE IF NOT EXISTS dashboard;
```

2. Arranca el backend.

3. Fuerza una sincronizacion manual:

```powershell
curl.exe -X POST http://localhost:8080/aruba/sync-aps
```

4. Comprueba la tabla:

```sql
SELECT * FROM dashboard.access_points;
```

5. Comprueba tambien el endpoint que lee desde MySQL:

```powershell
curl.exe http://localhost:8080/aruba/stored-aps
```

## KPIs del Dashboard

El frontend muestra:

- Total APs.
- APs activos.
- APs caidos.
- Sites.
- Swarms.
- Firmware pendiente.
- APs sin IP publica.
- APs inactivos.
- Estado general de red.

## Verificacion

Backend:

```powershell
cd dashboard
mvn -q -DskipTests compile
mvn test
```

Frontend:

```powershell
cd frontend
npm run build
```
