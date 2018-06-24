package io.bootique.swagger.ui;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

/**
 * @since  0.26
 */
public class SwaggerUiModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new SwaggerUiModule();
    }
}
