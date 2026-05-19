# Cuadro de Mandos

Cuadro de mandos para visualizar el estado de los puntos de acceso Aruba y mantener una copia actualizada en MySQL.

## Arquitectura

- `dashboard`: backend Spring Boot.
- `frontend`: interfaz React + Vite.
- `dashboard.access_points`: tabla MySQL con una fila por AP, identificada por numero de serie.
- `dashboard.aruba_switches`: tabla MySQL con una fila por switch Aruba, identificado por numero de serie.
- `dashboard.aruba_switch_client_usage`: tabla MySQL con interfaces en down agrupadas por switch.
- `dashboard.aruba_switch_interface_usage_history`: historico de interfaces en down por switch.
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

Listado de switches directo desde Aruba:

```http
GET http://localhost:8080/aruba/switches
```

Listado de switches guardado en MySQL:

```http
GET http://localhost:8080/aruba/stored-switches
```

Uso de interfaces en down por switch guardado en MySQL:

```http
GET http://localhost:8080/aruba/switch-client-usage
```

Listado de clientes WiFi conectado en vivo desde Aruba. No se guarda en MySQL:

```http
GET http://localhost:8080/aruba/wifi-clients
```

Sincronizacion manual de APs en MySQL:

```http
POST http://localhost:8080/aruba/sync-aps
```

Sincronizacion manual de switches en MySQL:

```http
POST http://localhost:8080/aruba/sync-switches
```

Sincronizacion manual de interfaces en down por switch:

```http
POST http://localhost:8080/aruba/sync-switch-client-usage
```

## Sincronizacion Automatica

El backend sincroniza APs, switches e interfaces en down automaticamente con `ArubaScheduler`:

- Primera ejecucion: configurable con `aruba.sync.initial-delay-ms`.
- Frecuencia: configurable con `aruba.sync.fixed-rate-ms`.

Ademas, `GET /aruba/summary` tambien sincroniza los APs usando la misma lista que obtiene para calcular los KPIs.

Cada AP se guarda en `access_points` usando el numero de serie como clave logica. Cada switch se guarda en `aruba_switches` con el mismo criterio. `firstSeenAt` guarda cuando se vio por primera vez y no se sobrescribe; `lastSeenAt` se actualiza en cada sincronizacion.

Los switches se consultan en `GET /monitoring/v1/switches` y se guardan por numero de serie. Despues, para cada serial se consulta `GET /monitoring/v1/switches/{serial}/ports`, se cuentan los puertos con `status` igual a `down` y se guarda el ultimo estado en `aruba_switch_client_usage`. Cada sincronizacion tambien inserta una muestra en `aruba_switch_interface_usage_history`. En el dashboard solo se muestran como infrautilizados los switches que en los ultimos 30 dias han estado siempre `Up` y siempre con mas de 17 interfaces en `down`.

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
- Total switches.
- Switches apagados.
- Switches que necesitan upgrade de firmware.
- Switches infrautilizados con mas de 17 interfaces en `down` en los ultimos 30 dias.
- Clientes conectados en `MUTUALIA-APs`.
- Clientes conectados en `MUTUALIA-WIFI`.
- Clientes de `MUTUALIA-WIFI` separados por SSID:
  - `MUTUALIA_LANGILEAK`
  - `MUTUALIA`
  - `MUTUALIA_RED_INTERNA`
  - `MUTUALIA_RED_EXTERNA`
  - `MUTUALIA_KORPORATIBOA`
  - `WIFI_PACs`
  - `MUT_VIDEO`
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
