# CRM Multitenant SaaS

Arquitectura fullstack lista para producción con observabilidad Dynatrace.

## Stack

| Capa | Tecnología |
|------|------------|
| Auth | Keycloak 24 (OIDC) |
| Frontend / BFF | Next.js 15 (App Router) |
| Backend | Spring Boot 3.3 · Java 21 · WebMVC |
| Base de datos | PostgreSQL 16 (Schema-per-Tenant) |
| Tracing | Micrometer Tracing + W3C TraceContext |
| Observabilidad | Dynatrace (scrape automático) |
| Infraestructura | k3s + Argo CD |

## Estructura del proyecto

```
crm-multitenant-saas/
├── frontend/          # Next.js BFF (propaga traceparent, valida JWT)
├── backend/           # Spring Boot 3.3 (multitenancy, tracing, REST)
├── keycloak/          # Realm export para import automático
└── k8s/               # Manifiestos Kubernetes + Application ArgoCD
    ├── namespace.yaml
    ├── frontend/
    ├── backend/
    ├── keycloak/
    ├── postgres/
    └── argocd/
```

## Multitenancy: Schema-per-Tenant

Cada tenant tiene su propio esquema PostgreSQL (`tenant_<id>`).
El `tenant_id` se extrae del claim `tenant_id` del JWT de Keycloak
y se inyecta vía `CurrentTenantIdentifierResolver` de Hibernate.

```
Token JWT → TenantInterceptor → TenantContext (ThreadLocal)
                                      ↓
                         TenantIdentifierResolver
                                      ↓
                    Hibernate cambia schema en cada request
```

## Propagación de trazas W3C (traceparent)

```
Browser → Next.js BFF ──traceparent──→ Spring Boot
              ↑                              ↑
         Micrometer                    Micrometer Tracing
         (auto inject)               + OTEL Exporter (Dynatrace)
```

## Inicio rápido

```bash
# 1. Levantar infraestructura local
docker compose up -d postgres keycloak

# 2. Backend
cd backend && ./mvnw spring-boot:run

# 3. Frontend
cd frontend && npm install && npm run dev
```

## Deploy en k3s con ArgoCD

```bash
kubectl apply -f k8s/argocd/application.yaml
# ArgoCD sincroniza automáticamente desde /k8s
```
