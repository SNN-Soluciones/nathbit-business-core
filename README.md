# Nathbit Business Core

Microservicio de lógica de negocio para el sistema POS Nathbit. Maneja todas las operaciones de negocio del restaurante con arquitectura multi-tenant y auditoría completa en MongoDB.

---

## 📋 Tabla de Contenidos

- [Tecnologías](#tecnologías)
- [Arquitectura Multi-Tenant](#arquitectura-multi-tenant)
- [Autenticación de Doble Capa](#autenticación-de-doble-capa)
- [Auditoría Automática](#auditoría-automática)
- [Estructura de Base de Datos](#estructura-de-base-de-datos)
- [Endpoints Disponibles](#endpoints-disponibles)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Configuración](#configuración)

---

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.5.10**
- **Gradle 8.x**
- **PostgreSQL 15+** (Multi-tenant con schemas dinámicos)
- **MongoDB 6+** (Auditoría de eventos)
- **JWT** (Autenticación)
- **Lombok** (Reduce boilerplate)

---

## 🏗️ Arquitectura Multi-Tenant

### Separación de Datos

El sistema utiliza **schemas separados** en PostgreSQL para aislar datos por sucursal:

```
PostgreSQL Database: nathbitpos_v3
│
├── public/                          ← Datos GLOBALES (compartidos)
│   └── codigos_cabys                  Catálogo de códigos CAByS (Hacienda CR)
│
└── tenant_viaje_al_sabor/           ← Datos ESPECÍFICOS de cada sucursal
    ├── categorias_producto             Categorías de productos
    └── empresa_cabys                   Códigos CAByS asignados a esta sucursal
```

### ¿Cómo funciona el Multi-Tenant?

1. **Request llega** con `device-token` en el header
2. **DeviceTokenInterceptor** extrae el `tenantId` del token
3. **TenantContext** guarda `tenantId` en ThreadLocal
4. **TenantInterceptor** ejecuta: `SET search_path TO tenant_X, public`
5. **Hibernate** ahora busca automáticamente en el schema correcto
6. **Respuesta** se envía y contexto se limpia

```sql
-- Ejemplo: Si tenantId = "tenant_viaje_al_sabor"
SET search_path TO tenant_viaje_al_sabor, public;

-- Ahora este query:
SELECT * FROM categorias_producto;

-- Se ejecuta realmente como:
SELECT * FROM tenant_viaje_al_sabor.categorias_producto;
```

---

## 🔐 Autenticación de Doble Capa

El sistema requiere **DOS tokens** para cada operación:

### 1️⃣ Device Token (Nivel Dispositivo/Sucursal)

**Propósito:** Validar que el dispositivo puede acceder a este tenant

```http
Header: X-Device-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Claims del Device Token:**
```json
{
  "deviceId": "tablet-001",
  "deviceName": "Tablet Barra 1",
  "tenantId": "tenant_viaje_al_sabor",
  "exp": 1707350400
}
```

### 2️⃣ Bearer Token (Nivel Usuario)

**Propósito:** Identificar QUÉ usuario realiza la acción (para auditoría)

```http
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Claims del Bearer Token:**
```json
{
  "userId": 123,
  "username": "mesero_juan",
  "role": "MESERO",
  "exp": 1707350400
}
```

### 🔄 Flujo de Request

```
1. Request → DeviceTokenInterceptor
   ↓
2. Valida device-token → Extrae tenantId, deviceId, deviceName
   ↓
3. Poblar DeviceContext y TenantContext (ThreadLocal)
   ↓
4. Request → AuthInterceptor
   ↓
5. Valida Bearer token → Extrae userId, username, role
   ↓
6. Poblar UserContext (ThreadLocal)
   ↓
7. Request → TenantInterceptor
   ↓
8. SET search_path TO tenant_X, public
   ↓
9. Controller ejecuta lógica de negocio
   ↓
10. @Auditable guarda evento en MongoDB
   ↓
11. Response al cliente
   ↓
12. afterCompletion() limpia todos los contextos
```

---

## 📊 Auditoría Automática

Cada operación importante se registra automáticamente en **MongoDB** usando AOP.

### Estructura del Log de Auditoría

```javascript
// Colección: audit_logs
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "tenantId": "tenant_viaje_al_sabor",
  "timestamp": "2026-02-07T21:30:00Z",
  "action": "DELETE_CATEGORIA",
  
  // QUIÉN (del Bearer token)
  "userId": 123,
  "username": "mesero_juan",
  "userRole": "MESERO",
  
  // DESDE DÓNDE (del device-token)
  "deviceId": "tablet-001",
  "deviceName": "Tablet Barra 1",
  
  // QUÉ PASÓ
  "entityType": "CATEGORIA_PRODUCTO",
  "entityId": 5,
  "beforeState": {
    "id": 5,
    "nombre": "Bebidas",
    "activo": true
  },
  "afterState": {
    "id": 5,
    "nombre": "Bebidas",
    "activo": false  // ← Cambió a inactivo
  },
  
  // CONTEXTO
  "metadata": {
    "reason": "Categoría descontinuada"
  }
}
```

### Eventos Auditados

- ✅ `CREATE_CATEGORIA` - Crear categoría
- ✅ `UPDATE_CATEGORIA` - Actualizar categoría
- ✅ `DELETE_CATEGORIA` - Eliminar categoría
- ✅ `CHANGE_CATEGORIA_STATUS` - Activar/Desactivar categoría
- ✅ `CREATE_EMPRESA_CABYS` - Asignar código CAByS
- ✅ `DELETE_EMPRESA_CABYS` - Eliminar código CAByS
- 🔜 `DELETE_ORDER_LINE` - Eliminar línea de orden
- 🔜 `CREATE_DISCOUNT` - Crear descuento
- 🔜 `VOID_INVOICE` - Anular factura
- 🔜 `MODIFY_PRICE` - Modificar precio manual
- 🔜 `TRANSFER_TABLE` - Transferir mesa
- 🔜 `CLOSE_CASH_REGISTER` - Cerrar caja

---

## 🗄️ Estructura de Base de Datos

### Schema `public` (Global)

#### `codigos_cabys`
Catálogo completo de códigos CAByS de Hacienda (Costa Rica)

```sql
CREATE TABLE public.codigos_cabys (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(13) NOT NULL UNIQUE,    -- Ej: "6332000000000"
    descripcion TEXT NOT NULL,             -- Ej: "Suministro de comida..."
    impuesto_sugerido VARCHAR(100),        -- Ej: "13"
    activo BOOLEAN NOT NULL DEFAULT true
);
```

**Propósito:** Catálogo maestro compartido entre TODOS los tenants.

---

### Schema `tenant_X` (Específico por Sucursal)

#### `categorias_producto`
Categorías de productos del restaurante

```sql
CREATE TABLE tenant_viaje_al_sabor.categorias_producto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,          -- Ej: "Bebidas"
    descripcion VARCHAR(200),              -- Ej: "Bebidas calientes y frías"
    color VARCHAR(7),                      -- Ej: "#FF5733"
    icono VARCHAR(50),                     -- Ej: "fas fa-coffee"
    orden INTEGER NOT NULL,                -- Para ordenar en el menú
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**Propósito:** Organizar productos en categorías para el menú del POS.

---

#### `empresa_cabys`
Códigos CAByS asignados a esta sucursal

```sql
CREATE TABLE tenant_viaje_al_sabor.empresa_cabys (
    id BIGSERIAL PRIMARY KEY,
    
    -- Referencia al catálogo global
    codigo_cabys_id BIGINT NOT NULL,       -- FK a public.codigos_cabys.id
    
    -- Datos DUPLICADOS (sin JOINs, mejor performance)
    codigo VARCHAR(13) NOT NULL,           -- Ej: "6332000000000"
    descripcion TEXT NOT NULL,             -- Ej: "Suministro de comida..."
    impuesto_sugerido VARCHAR(100),        -- Ej: "13"
    
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL
);
```

**Propósito:** Cada sucursal selecciona qué códigos CAByS usa. Los datos se duplican desde `public.codigos_cabys` para evitar JOINs entre schemas.

**Flujo de Asignación:**
1. Usuario selecciona códigos CAByS desde el catálogo global (`public.codigos_cabys`)
2. Backend COPIA los datos a `tenant_X.empresa_cabys`
3. Ahora la sucursal tiene sus propios códigos CAByS sin necesidad de JOINs

---

## 🌐 Endpoints Disponibles

### Puerto: `8081`
### Base URL: `http://localhost:8081/api/business`

---

### 📦 Categorías de Productos

#### Listar Categorías Activas
```http
GET /categorias/activas
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>

Response 200:
[
  {
    "id": 1,
    "nombre": "Bebidas",
    "descripcion": "Bebidas calientes y frías",
    "color": "#FF5733",
    "icono": "fas fa-coffee",
    "orden": 1,
    "activo": true,
    "createdAt": "2026-02-07T10:00:00",
    "updatedAt": "2026-02-07T10:00:00"
  }
]
```

#### Crear Categoría
```http
POST /categorias
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>
Body:
{
  "nombre": "Postres",
  "descripcion": "Postres y dulces",
  "color": "#FFD700",
  "icono": "fas fa-ice-cream",
  "orden": 5
}

Response 201:
{
  "id": 8,
  "nombre": "Postres",
  ...
}
```

#### Actualizar Categoría
```http
PUT /categorias/{id}
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>
Body:
{
  "nombre": "Postres Fríos",
  "descripcion": "Helados y postres fríos",
  "color": "#00CED1",
  "icono": "fas fa-snowflake",
  "orden": 6
}

Response 200:
{
  "id": 8,
  "nombre": "Postres Fríos",
  ...
}
```

#### Activar/Desactivar Categoría
```http
PATCH /categorias/{id}/estado?activo=false
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>

Response 200:
{
  "id": 8,
  "nombre": "Postres",
  "activo": false,
  ...
}
```

#### Eliminar Categoría (Soft Delete)
```http
DELETE /categorias/{id}
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>

Response 204: No Content
```

---

### 🏷️ Códigos CAByS de la Empresa

#### Listar CAByS Asignados
```http
GET /empresa-cabys/activos
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>

Response 200:
[
  {
    "id": 1,
    "codigoCabysId": 14832,
    "codigo": "6332000000000",
    "descripcion": "Suministro de comida, servicio de restaurante sin mesero",
    "impuestoSugerido": "13",
    "activo": true,
    "createdAt": "2025-09-02T11:15:45"
  }
]
```

#### Asignar Código CAByS
```http
POST /empresa-cabys
Headers:
  X-Device-Token: <device-token>
  Authorization: Bearer <bearer-token>
Body:
{
  "codigoCabysId": 14832,
  "codigo": "6332000000000",
  "descripcion": "Suministro de comida, servicio de restaurante sin mesero",
  "impuestoSugerido": "13"
}

Response 201:
{
  "id": 8,
  "codigoCabysId": 14832,
  "codigo": "6332000000000",
  ...
}
```

---

## 💡 Ejemplos de Uso

### Ejemplo 1: Crear Categoría desde Angular

```typescript
// Angular Service
createCategoria(categoria: CreateCategoriaDTO): Observable<CategoriaDTO> {
  const headers = {
    'X-Device-Token': this.deviceTokenService.getToken(),
    'Authorization': `Bearer ${this.authService.getBearerToken()}`
  };
  
  return this.http.post<CategoriaDTO>(
    'http://localhost:8081/api/business/categorias',
    categoria,
    { headers }
  );
}
```

### Ejemplo 2: Asignar CAByS desde Frontend

```typescript
// 1. Usuario busca en el catálogo global (public.codigos_cabys)
buscarCodigos(query: string): Observable<CodigoCabys[]> {
  return this.http.get<CodigoCabys[]>(
    `http://localhost:8081/api/business/codigos-cabys/buscar?q=${query}`
  );
}

// 2. Usuario selecciona un código
// 3. Frontend envía la asignación
asignarCabys(cabys: CodigoCabys): Observable<EmpresaCabysDTO> {
  const dto = {
    codigoCabysId: cabys.id,
    codigo: cabys.codigo,
    descripcion: cabys.descripcion,
    impuestoSugerido: cabys.impuestoSugerido
  };
  
  return this.http.post<EmpresaCabysDTO>(
    'http://localhost:8081/api/business/empresa-cabys',
    dto,
    { headers: this.getHeaders() }
  );
}
```

### Ejemplo 3: Ver Auditoría en MongoDB

```javascript
// MongoDB Query
db.audit_logs.find({
  tenantId: "tenant_viaje_al_sabor",
  action: "DELETE_CATEGORIA",
  timestamp: {
    $gte: ISODate("2026-02-07T00:00:00Z"),
    $lt: ISODate("2026-02-08T00:00:00Z")
  }
}).sort({ timestamp: -1 })

// Resultado:
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "action": "DELETE_CATEGORIA",
  "userId": 123,
  "username": "mesero_juan",
  "deviceId": "tablet-001",
  "beforeState": { "activo": true },
  "afterState": { "activo": false }
}
```

---

## ⚙️ Configuración

### application.yml

```yaml
server:
  port: 8081
  servlet:
    context-path: /api/business

spring:
  application:
    name: nathbit-business-core

  # PostgreSQL (Multi-tenant)
  datasource:
    url: jdbc:postgresql://localhost:5432/nathbitpos_v3
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: public

  # MongoDB (Auditoría)
  data:
    mongodb:
      uri: mongodb://localhost:27017/nathbit_audit

# JWT
jwt:
  secret: your-secret-key-change-this-in-production
  device-token:
    expiration: 86400000  # 24 horas
  bearer-token:
    expiration: 3600000   # 1 hora
```

---

## 🚀 Ejecutar el Proyecto

```bash
# Clonar repositorio
git clone <repo-url>
cd nathbit-business-core

# Compilar
./gradlew build

# Ejecutar
./gradlew bootRun

# Verificar
curl http://localhost:8081/api/business/actuator/health
```

---

## 📝 Próximos Pasos

- [ ] Implementar DeviceTokenInterceptor
- [ ] Implementar AuthInterceptor (Bearer Token)
- [ ] Configurar Spring Security
- [ ] Implementar AOP para auditoría (@Auditable)
- [ ] Crear entidades de Productos, Mesas, Órdenes
- [ ] Implementar endpoints de facturación
- [ ] Integrar con nathbit-mh-processor (Factura electrónica)

---

## 📞 Contacto

**Desarrollador:** Andrés  
**Empresa:** SNN Soluciones  
**Proyecto:** Nathbit POS v2