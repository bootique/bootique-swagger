package io.bootique.swagger;

import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.test.junit.JettyTestFactory;
import io.swagger.annotations.Api;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class SwaggerModuleIT {

    @ClassRule
    public static JettyTestFactory testFactory = new JettyTestFactory();

    private static WebTarget BASE_TARGET = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BeforeClass
    public static void beforeClass() {
        testFactory.app()
                .modules(JettyModule.class, JerseyModule.class, SwaggerModule.class)
                .module(binder -> JerseyModule.contributeResources(binder).addBinding().to(TestApi.class))
                .start();
    }

    @Test
    public void testApi_Yaml() {

        Response r = BASE_TARGET.path("/swagger.yaml").request().get();
        assertEquals(200, r.getStatus());
        assertEqualsToResourceContents("response1.yml", r.readEntity(String.class));
    }

    @Test
    public void testApi_Json() {

        Response r = BASE_TARGET.path("/swagger.json").request().get();
        assertEquals(200, r.getStatus());
        assertEqualsToResourceContents("response1.json", r.readEntity(String.class) + "\n");
    }

    private void assertEqualsToResourceContents(String expectedResource, String toTest) {

        try (Scanner scanner = new Scanner(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(expectedResource)), "UTF-8")) {

            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
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
        @Path("/sub/{id}")
        public String subget(@PathParam("id") int id) {
            return "get_" + id;
        }
    }
}
