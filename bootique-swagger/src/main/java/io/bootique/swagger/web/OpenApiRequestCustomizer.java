package io.bootique.swagger.web;

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
     * Customizes the provided copy of the OpenAPI. Passing OpenAPI instance as a Supplier would avoid unneeded cloning
     * of the shared instance in case the customizer decides to leave it unchanged.
     */
    void customize(HttpServletRequest request, Supplier<OpenAPI> api);
}
