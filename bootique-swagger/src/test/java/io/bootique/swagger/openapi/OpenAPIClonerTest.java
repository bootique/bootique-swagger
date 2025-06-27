package io.bootique.swagger.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAPIClonerTest {

    @Test
    void cloneOpenAPI() {
        OpenAPI original = new OpenAPI()
                .openapi("3.1")
                .addServersItem(new Server().description("sd").url("localhost"))
                .paths(new Paths().addPathItem("p1", new PathItem().get(new Operation().operationId("_get_"))));

        OpenAPI clone1 = OpenAPICloner.cloneOpenAPI(original);
        OpenAPI clone2 = OpenAPICloner.cloneOpenAPI(original);

        assertNotSame(original, clone1);
        assertNotSame(original, clone2);
        assertNotSame(clone1, clone2);

        assertEquals("3.1", clone1.getOpenapi());

        assertNotSame(original.getServers().get(0), clone1.getServers().get(0));
        assertEquals("sd", clone1.getServers().get(0).getDescription());
        assertEquals("localhost", clone1.getServers().get(0).getUrl());

        assertNotSame(original.getPaths(), clone1.getPaths());
        assertNotSame(original.getPaths().get("p1"), clone1.getPaths().get("p1"));
        assertEquals("_get_", clone1.getPaths().get("p1").getGet().getOperationId());
    }
}
