# Intranet Distribución - Backend API

Backend desarrollado en **Spring Boot 3** con arquitectura por capas, preparado para conectarse a **PostgreSQL (Supabase)**. Este sistema gestiona los módulos de logística, pedidos, almacenes, flota y seguimiento en tiempo real para la intranet de distribución.

---

## Arquitectura del Proyecto

El proyecto sigue una **Arquitectura por Capas (Layered Architecture)** para garantizar un alto grado de desacoplamiento, mantenibilidad y facilidad de prueba.

```text
com.dwi.intranetdistribucion
│
├── config/           # Configuraciones globales (CORS, Seguridad, Swagger/OpenAPI, Beans)
├── controller/       # Capa de Presentación (Controladores REST que exponen los endpoints)
├── dto/              # Data Transfer Objects (Objetos de transferencia para Request y Response)
│   ├── request/
│   └── response/
├── exception/        # Manejo global de excepciones (@RestControllerAdvice)
├── model/            # Capa de Dominio / Entidades JPA (Mapeo a la base de datos)
├── repository/       # Capa de Acceso a Datos (Spring Data JPA Repositories)
├── service/          # Capa de Servicio (Interfaces con contratos de lógica de negocio)
│   └── impl/         # Implementaciones concretas de la lógica de negocio
└── util/             # Clases utilitarias (Constantes, Helpers, Formateadores)
```

---

## Requisitos Previos

- **Java JDK:** 17 o 23
- **Gestor de paquetes:** Gradle (incluido mediante `./gradlew`)
- **Base de Datos:** PostgreSQL 15+ (o instancia en Supabase)

---

## Configuración del Entorno

Configurar las credenciales de la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://<TU-HOST-SUPABASE>:5432/postgres?sslmode=require
spring.datasource.username=<TU-USUARIO-POSTGRES>
spring.datasource.password=<TU-PASSWORD>
```

---

## Comandos de Desarrollo

```powershell
# Compilar el proyecto
.\gradlew compileJava

# Ejecutar las pruebas unitarias
.\gradlew test

# Iniciar el servidor en modo desarrollo (Puerto 8080)
.\gradlew bootRun
```

---

## Mapa de Módulos y Requerimientos Funcionales (Issues)

A continuación se detallan los módulos planeados y mapeados según el backlog del proyecto:

### 1. Seguridad y Acceso

- `RF-01`: Módulo de inicio de sesión (JWT / Auth)
- `RF-02`: Módulo de roles y permisos (RBAC)

### 2. Administración Base

- `RF-03`: Gestión de usuarios
- `RF-04`: Gestión de sedes
- `RF-05`: Gestión de almacenes

### 3. Catálogo e Inventario

- `RF-06`: Gestión de productos
- `RF-07`: Gestión de categorías
- `RF-08`: Control de stock e inventario

### 4. Pedidos y Clientes

- `RF-09`: Gestión de clientes
- `RF-10`: Generación de pedidos
- `RF-11`: Aprobación de pedidos
- `RF-12`: Facturación y guías de remisión

### 5. Rutas y Logística de Distribución

- `RF-13`: Zonas de reparto
- `RF-14`: Rutas de distribución
- `RF-15`: Asignación de pedidos a rutas
- `RF-16`: Seguimiento de entregas
- `RF-17`: Incidencias de entrega

### 6. Flota y Conductores

- `RF-18`: Gestión de vehículos
- `RF-19`: Gestión de conductores
- `RF-20`: Mantenimiento de flota
- `RF-21`: Asignación vehículo-conductor-ruta

### 7. Reportes y Analítica

- `RF-22`: Reporte de inventario (PDF/Excel)
- `RF-23`: Reporte de pedidos
- `RF-24`: Reporte de entregas y cumplimiento
- `RF-25`: Reporte de productividad de flota
- `RF-26`: Dashboard general de KPIs logísticos

---

## Licencia y Autoría

Desarrollado por el equipo de **Desarrollo Web Integrado - UTP**.

- Navarro Domínguez, Rommel Eduardo
- Pujay Narciso, Alinder Steeven
- Alvarado Chávez, Romina Liz
- Herrera Gutierreez, Mathias Sebastian
