package io.bootique.swagger;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.swagger.jaxrs.listing.ApiListingResource;

import java.io.File;

public class SwaggerModule extends ConfigModule {

    @Override
    public void configure(Binder binder) {
        File currentJavaJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        BQCoreModule.extend(binder).setProperty("bq.jetty.servlets.swagger-ui.params.resourceBase",  "jar:file:" + currentJavaJarFile.getPath() + "!/");
        JettyModule.extend(binder).addStaticServlet("swagger-ui", "/swagger-ui/*");
        JerseyModule.extend(binder).addPackage(ApiListingResource.class);
    }
}
