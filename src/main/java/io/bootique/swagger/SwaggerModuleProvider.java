package io.bootique.swagger;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jersey.JerseyModuleProvider;

import java.util.Collection;
import java.util.Collections;

public class SwaggerModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new SwaggerModule();
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return Collections.singletonList(
                new JerseyModuleProvider()
        );
    }
}
