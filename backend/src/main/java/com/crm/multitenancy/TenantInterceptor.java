package com.crm.multitenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor WebMVC que:
 * 1. Extrae tenant_id del claim JWT de Keycloak.
 * 2. Lo almacena en TenantContext (ThreadLocal).
 * 3. Lo limpia al finalizar el request (afterCompletion).
 *
 * El claim 'tenant_id' se configura en Keycloak como User Attribute
 * mapeado a Token Claim con el mismo nombre.
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_CLAIM = "tenant_id";
    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String tenantId = jwt.getClaimAsString(TENANT_CLAIM);
            if (tenantId != null && !tenantId.isBlank()) {
                // Sanitize: sólo alfanumérico + guiones
                String safe = tenantId.replaceAll("[^a-zA-Z0-9_-]", "");
                TenantContext.set("tenant_" + safe);
                log.debug("[Tenant] schema=tenant_{} traceId={}",
                        safe, request.getHeader("traceparent"));
            } else {
                TenantContext.set(DEFAULT_SCHEMA);
                log.warn("[Tenant] JWT sin claim '{}', usando schema public", TENANT_CLAIM);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
