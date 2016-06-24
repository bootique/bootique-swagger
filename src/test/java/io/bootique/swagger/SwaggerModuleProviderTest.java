package io.bootique.swagger;

import com.nhl.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class SwaggerModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(SwaggerModuleProvider.class);
    }
}
