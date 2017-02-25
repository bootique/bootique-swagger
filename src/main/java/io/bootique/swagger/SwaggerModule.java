package io.bootique.swagger;

import com.google.inject.Binder;
import io.bootique.ConfigModule;
import io.bootique.jersey.JerseyModule;
import io.swagger.jaxrs.listing.ApiListingResource;

public class SwaggerModule extends ConfigModule {

    @Override
    public void configure(Binder binder) {
        JerseyModule.extend(binder).addPackage(ApiListingResource.class);
    }
}
