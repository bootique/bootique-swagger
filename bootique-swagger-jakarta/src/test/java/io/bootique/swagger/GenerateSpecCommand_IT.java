package io.bootique.swagger;

import io.bootique.jersey.JerseyModule;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.resource.ResourceFactory;
import io.bootique.swagger.config3.Api31;
import io.bootique.swagger.config3a.Api3a;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
public class GenerateSpecCommand_IT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @TempDir
    public Path destDir;

    @Test
    void noWebAccess() throws Exception {
        var result = testFactory.app(buildArgs("config1/startup-no-web-access.yml"))
                .autoLoadModules()
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("s1_model.json", "config1/response.json");
        checkFiles("s1_model.yaml", "config1/response.yml");
    }

    @Test
    void annotationSpec_yamlOverrideSpec() throws Exception {
        var result = testFactory.app(buildArgs("config1/startup.yml"))
                .autoLoadModules()
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("s1_model.json", "config1/response.json");
        checkFiles("s1_model.yaml", "config1/response.yml");
    }

    @Test
    void annotationSpecs_classFilter() throws Exception {
        var result = testFactory.app(buildArgs("config3/startup1.yml"))
                .autoLoadModules()
                .module(b -> JerseyModule.extend(b)
                        .addPackage(Api31.class))
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("c3_model.yaml", "config3/response1.yml");
    }

    @Test
    void annotationSpecs_packageFilter() throws Exception {
        var result = testFactory.app(buildArgs("config3/startup2.yml"))
                .autoLoadModules()
                .module(b -> JerseyModule.extend(b)
                        .addPackage(Api31.class)
                        .addPackage(Api3a.class))
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("c3_model.yaml", "config3/response2.yml");
    }

    @Test
    void yamlSpec_sharedYamlOverrideSpec() throws Exception {
        var result = testFactory.app(buildArgs("config4/startup.yml"))
                .autoLoadModules()
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("c1_model.json", "config4/response1.json");
        checkFiles("c2_model.json", "config4/response2.json");
    }

    @Test
    void yamlSpec_yamlOverrideSpec() throws Exception {
        var result = testFactory.app(buildArgs("config2/startup.yml"))
                .autoLoadModules()
                .run();
        assertTrue(result.isSuccess(), "Result should be success.");
        checkFiles("s2_model.yaml", "config2/response.yml");
        checkFiles("s2_model.json", "config2/response.json");
    }


    private String[] buildArgs(String configPath) {
        return new String[]{"--generate-spec", "--config=classpath:" + configPath, "--d=" + destDir};
    }

    private void checkFiles(String fileName, String expectedFilePath) throws Exception {
        var resolved = destDir.resolve(fileName);
        assertTrue(Files.exists(resolved),  resolved + " should exist.");
        var expected = resourceContent("classpath:" + expectedFilePath);
        var actual = resourceContent(resolved.toUri().toURL().toString());
        assertEquals(expected, actual);

    }

    private String resourceContent(String resourceId) {
        var resource = new ResourceFactory(resourceId);
        URL url = resource.getUrl();

        try (InputStream in = url.openStream()) {
            // read as bytes to preserve line breaks
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                out.write(data, 0, nRead);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource contents: " + url, e);
        }
    }
}
