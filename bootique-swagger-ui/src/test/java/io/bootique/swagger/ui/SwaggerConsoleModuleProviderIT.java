package io.bootique.swagger.ui;

import io.bootique.BQRuntime;
import io.bootique.swagger.ui.SwaggerConsoleModule;
import io.bootique.swagger.ui.SwaggerConsoleModuleProvider;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class SwaggerConsoleModuleProviderIT {
    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(SwaggerConsoleModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new SwaggerConsoleModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                SwaggerConsoleModule.class
        );
    }
}
