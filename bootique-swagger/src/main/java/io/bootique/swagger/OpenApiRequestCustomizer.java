package io.bootique.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Supplier;

/**
 * Per-request in-place customizer of the OpenAPI model object.
 *
 * @since 4.0
 */
public interface OpenApiRequestCustomizer {

    /**
     * A callback invoked by Bootique to allow this customizer to inspect and alter the provided Open API model.
     * Calling "apiSupplier.get()" within the customizer returns a mutable copy of the {@link OpenAPI}
     * object that can be changed directly. Passing OpenAPI instance as a Supplier would avoid unneeded cloning
     * of the shared instance in case the customizer decides to leave it unchanged.
     */
    void customize(HttpServletRequest request, Supplier<OpenAPI> apiSupplier);
}
