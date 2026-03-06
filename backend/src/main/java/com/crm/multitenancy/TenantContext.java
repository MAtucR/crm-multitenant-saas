package com.crm.multitenancy;

/**
 * ThreadLocal que almacena el tenant_id extraído del JWT.
 * Se establece en TenantInterceptor (pre-handle) y se limpia en afterCompletion.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
