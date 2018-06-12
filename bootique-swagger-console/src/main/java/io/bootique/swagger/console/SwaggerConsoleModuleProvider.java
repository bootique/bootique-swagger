package io.bootique.swagger.console;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

/**
 * @since  0.26
 */
public class SwaggerConsoleModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new SwaggerConsoleModule();
    }
}
