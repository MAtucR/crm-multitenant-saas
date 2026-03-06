package com.crm.multitenancy;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementa CurrentTenantIdentifierResolver de Hibernate.
 * Hibernate llama a resolveCurrentTenantIdentifier() en cada sesión JPA
 * para saber qué schema usar.
 *
 * Registrado como HibernatePropertiesCustomizer para inyectarlo
 * sin romper el auto-configure de Spring Boot.
 */
@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final String DEFAULT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // true: Hibernate valida que sesiones en cache pertenezcan al tenant actual
        return true;
    }

    /** Registra el resolver en las properties de Hibernate sin XML. */
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
