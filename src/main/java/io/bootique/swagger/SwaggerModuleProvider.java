package io.bootique.swagger;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class SwaggerModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new SwaggerModule();
    }
}
