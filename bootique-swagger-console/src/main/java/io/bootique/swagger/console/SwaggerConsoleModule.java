package io.bootique.swagger.console;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.jetty.JettyModule;
import io.bootique.swagger.SwaggerModule;

import java.io.File;

public class SwaggerConsoleModule extends SwaggerModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        File currentJavaJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        BQCoreModule.extend(binder).setProperty("bq.jetty.servlets.swagger-ui.params.resourceBase", "jar:file:" + currentJavaJarFile.getPath() + "!/");
        JettyModule.extend(binder).addStaticServlet("swagger-ui", "/swagger-ui/*");

    }
}
