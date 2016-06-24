package io.bootique.swagger;

import com.google.inject.Binder;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.jersey.JerseyModule;
import io.swagger.jaxrs.listing.ApiListingResource;

public class SwaggerModule extends ConfigModule {

    @Override
    public void configure(Binder binder) {
        JerseyModule.contributePackages(binder).addBinding().toInstance(ApiListingResource.class.getPackage());
    }
}
