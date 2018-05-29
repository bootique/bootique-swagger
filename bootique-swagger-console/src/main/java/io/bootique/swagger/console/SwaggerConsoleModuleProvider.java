package io.bootique.swagger.console;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class SwaggerConsoleModuleProvider implements BQModuleProvider {
    @Override
    public Module module() {
        return new SwaggerConsoleModule();
    }
}
