package io.bootique.swagger;

import io.bootique.di.Binder;
import io.swagger.v3.oas.models.PathItem;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A custom per-request filter for OpenAPI models. Removes paths and schemas that do not match the filter. Installed via
 * {@link SwaggerModule#extend(Binder)}. This is normally used to apply per-request permissions to the OpenAPI model.
 *
 * @since 4.0
 */
@FunctionalInterface
public interface OpenApiModelFilter {

    boolean shouldInclude(HttpServletRequest request, String path, PathItem.HttpMethod method);
}
