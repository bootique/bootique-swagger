package io.bootique.swagger;

import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.jetty.test.junit.JettyTestFactory;
import io.swagger.annotations.Api;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.Objects;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class SwaggerModuleIT {

    private static WebTarget BASE_TARGET = ClientBuilder.newClient().target("http://127.0.0.1:8080/");
    @Rule
    public JettyTestFactory testFactory = new JettyTestFactory();

    @Test
    public void testApi() {

        testFactory.newRuntime().configurator(bootique -> {
            bootique.modules(JettyModule.class, JerseyModule.class, SwaggerModule.class)
                    .module(binder -> JerseyModule.contributeResources(binder).addBinding().to(TestApi.class));
        }).startServer();

        Response r = BASE_TARGET.path("/swagger.yaml").request().get();
        assertEquals(200, r.getStatus());
        assertEqualsToResourceContents("response1.yml", r.readEntity(String.class));
    }

    private void assertEqualsToResourceContents(String expectedResource, String toTest) {

        try (Scanner scanner = new Scanner(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(expectedResource)), "UTF-8")) {

            StringBuilder builder = new StringBuilder();
            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine()).append("\n");
            }

            assertEquals(builder.toString(), toTest);
        }
    }

    @Api
    @Path("/")
    public static class TestApi {

        @GET
        public String get() {
            return "get_";
        }

        @GET
        @Path("/sub")
        public String subget() {
            return "get_";
        }
    }
}
