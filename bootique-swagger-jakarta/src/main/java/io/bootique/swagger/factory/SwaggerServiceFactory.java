package io.bootique.swagger.factory;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.OpenApiCustomizer;
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerService;
import io.bootique.swagger.converter.LocalTimeConverter;
import io.bootique.swagger.converter.YearConverter;
import io.bootique.swagger.converter.YearMonthConverter;
import io.bootique.swagger.converter.ZoneOffsetConverter;
import io.bootique.swagger.customizer.PathSortingCustomizer;
import io.bootique.swagger.customizer.SchemasSortingCustomizer;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;

import javax.inject.Inject;
import java.util.*;

@BQConfig
public class SwaggerServiceFactory {

    private final Set<ModelConverter> converters;
    private final Set<OpenApiCustomizer> diCustomizers;

    private ResourceFactory overrideSpec;
    private Map<String, OpenApiModelFactory> specs;
    private boolean pretty = true;

    @Inject
    public SwaggerServiceFactory(Set<ModelConverter> converters, Set<OpenApiCustomizer> diCustomizers) {
        this.converters = converters;
        this.diCustomizers = diCustomizers;
    }

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

    public SwaggerService create() {

        // side effect of creating SwaggerService is installing ModelConverters
        // TODO: suggest Swagger to tie converters to contexts instead of using static ModelConverters
        installConverters(converters);

        return new SwaggerService(createModels(specs, prepareCustomizers()));
    }

    private List<OpenApiCustomizer> prepareCustomizers() {

        // start with standard customizers, so that the user-provided ones can fix the model to
        // their liking

        List<OpenApiCustomizer> customizers = new ArrayList<>(2 + diCustomizers.size());
        customizers.add(new PathSortingCustomizer());
        customizers.add(new SchemasSortingCustomizer());
        customizers.addAll(diCustomizers);

        return customizers;
    }

    private Map<String, OpenApiModel> createModels(
            Map<String, OpenApiModelFactory> specs,
            List<OpenApiCustomizer> customizers) {

        Map<String, OpenApiModel> models = new HashMap<>();

        Map<String, OpenApiModelFactory> localSpecs = specs != null ? specs : new HashMap<>();
        localSpecs.forEach((n, f) -> loadModel(models, n, f, customizers));

        return models;
    }

    private void loadModel(
            Map<String, OpenApiModel> models,
            String name,
            OpenApiModelFactory factory,
            List<OpenApiCustomizer> customizers) {
        factory.createModel(name, overrideSpec, pretty, customizers).ifPresent(m -> indexByPath(models, m));
    }

    private void indexByPath(Map<String, OpenApiModel> models, OpenApiModel model) {
        if (model.getPathJson() != null) {
            models.put(model.getPathJson(), model);
        }

        if (model.getPathYaml() != null) {
            models.put(model.getPathYaml(), model);
        }
    }

    private static void installConverters(Set<ModelConverter> converters) {

        // Internally "ModelConverters.addConverter()" inserts each converter in the beginning of the list
        // So the order of addition (standard first, then custom) allows custom injected converters to override the
        // standard ones.

        ModelConverters mc = ModelConverters.getInstance();

        // standard converters
        mc.addConverter(new YearMonthConverter());
        mc.addConverter(new YearConverter());
        mc.addConverter(new LocalTimeConverter());
        mc.addConverter(new ZoneOffsetConverter());

        // custom injected converters
        for (ModelConverter c : converters) {

            // since ModelConverters is a static singleton, lets at least make an attempt to prevent multiple
            // registrations of the same converter. Those "contains" checks are rather weak though.
            if (!mc.getConverters().contains(c)) {
                mc.addConverter(c);
            }
        }
    }
}
