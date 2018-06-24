package io.bootique.swagger.console;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.jetty.JettyModule;

import java.net.URL;

/**
 * @since  0.26
 */
public class SwaggerConsoleModule extends ConfigModule {

    public static final String RESOURCE_BASE = "bq.jetty.servlets.swagger-ui.params.resourceBase";

    @Override
    public void configure(Binder binder) {
        URL resource = this.getClass().getClassLoader().getResource("console/");
        BQCoreModule.extend(binder).setProperty(RESOURCE_BASE, resource.toString());
        JettyModule.extend(binder).addStaticServlet("swagger-ui", "/swagger-ui/*");
    }
}
