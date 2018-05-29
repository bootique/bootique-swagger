package io.bootique.swagger;

import com.google.inject.Binder;
import io.bootique.ConfigModule;
import io.bootique.jersey.JerseyModule;
import io.swagger.jaxrs.listing.ApiListingResource;

import javax.validation.constraints.NotNull;

public class SwaggerModule extends ConfigModule {

    @Override
    public void configure(@NotNull final Binder binder) {
        JerseyModule.extend(binder).addPackage(ApiListingResource.class);
    }
}
