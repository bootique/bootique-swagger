package io.bootique.swagger.factory;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@BQConfig
public class SwaggerServiceFactory {
    // shared spec
    private ResourceFactory overrideSpec;
    private Map<String, OpenApiModelFactory> specs;
    private boolean pretty = true;

    @BQConfigProperty("Zero or more API specifications provided by the application")
    public void setSpecs(Map<String, OpenApiModelFactory> specs) {
        this.specs = specs;
    }

    @BQConfigProperty("Location of the OpenAPI spec file, that overrides 'spec', 'resourcePackages', 'resourceClasses' models. " +
            "This setting is shared by all child specs, unless they define an explicit 'overrideSpec' of their own")
    public void setOverrideSpec(ResourceFactory overrideSpec) {
        this.overrideSpec = overrideSpec;
    }

    @BQConfigProperty("Whether to format YAML and JSON. 'True' by default. This setting is shared by all child specs.")
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    public SwaggerService createSwaggerService() {
        var models = createModels(this.specs);
        return new SwaggerService(models);
    }

    private Map<String, OpenApiModel> createModels(Map<String, OpenApiModelFactory> specs) {
        Map<String, OpenApiModel> models = new HashMap<>();
        if (specs == null) {
            specs = new HashMap<>();
        }
        specs.values().stream()
                .map(swaggerSpec -> swaggerSpec.createModel(overrideSpec, pretty))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(m -> indexByPath(models, m));
        return models;
    }

    private void indexByPath(Map<String, OpenApiModel> models, OpenApiModel model) {
        if (model.getPathJson() != null) {
            models.put(model.getPathJson(), model);
        }

        if (model.getPathYaml() != null) {
            models.put(model.getPathYaml(), model);
        }
    }
}
