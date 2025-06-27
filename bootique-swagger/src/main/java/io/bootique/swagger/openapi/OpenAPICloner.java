package io.bootique.swagger.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Objects;

public class OpenAPICloner {

    public static OpenAPI cloneOpenAPI(OpenAPI api) {

        Objects.requireNonNull(api);

        // Cloning OpenAPI tree algorithmically is fragile and unmaintainable. There's an infinite and constantly
        // growing number of properties, subclasses, etc. Instead, doing a brute-force clone via (de)serialization

        try {
            String json = Json.mapper().writeValueAsString(api);
            return Json.mapper().readValue(json, OpenAPI.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error cloning OpenAPI via (de)serialization", e);
        }
    }

}
