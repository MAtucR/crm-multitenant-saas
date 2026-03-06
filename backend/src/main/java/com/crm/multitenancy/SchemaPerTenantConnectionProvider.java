package com.crm.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER;

/**
 * Cambia el search_path de PostgreSQL en cada conexión según el tenant.
 * Hibernate llama a getConnection(tenantIdentifier) cuando necesita
 * abrir una sesión JPA.
 *
 * Estrategia: SET search_path = tenant_<id>, public
 * Esto permite que las tablas comunes (ej. system_settings) sigan en public.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaPerTenantConnectionProvider
        implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            // Crea el schema si no existe (útil en dev; en prod usar migraciones)
            conn.createStatement().execute(
                    "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");
            conn.createStatement().execute(
                    "SET search_path = \"" + schema + "\", public");
            log.debug("[Tenant] search_path={}", schema);
        } catch (SQLException e) {
            log.error("[Tenant] Error al cambiar schema a '{}': {}", schema, e.getMessage());
            conn.close();
            throw e;
        }
        return conn;
    }

    @Override
    public void releaseConnection(String schema, Connection connection) throws SQLException {
        try {
            connection.createStatement().execute("SET search_path = public");
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void customize(Map<String, Object> props) {
        props.put(MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
