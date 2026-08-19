/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Tests tag removal against the parts of the model that the annotation scanner does not normally produce, and hence
 * can't be tested via {@link OpenApiModelRequestFilterIT}.
 */
public class OpenApiModelFilterCustomizerTest {

    private static OpenAPI apiWithTagsInEveryOperationHolder() {

        // "kept" and "removed" are in "paths" and are subject to filtering. The rest of the tags are attached to
        // operations that are not reachable via "paths", and so must survive the filtering

        Operation kept = new Operation().tags(List.of("kept"))
                .callbacks(callback("keptCallback"));

        Operation removed = new Operation().tags(List.of("removed"))
                .callbacks(callback("removedCallback"));

        return new OpenAPI()
                .paths((Paths) new Paths()
                        .addPathItem("/kept", new PathItem().get(kept))
                        .addPathItem("/removed", new PathItem().get(removed)))
                .webhooks(Map.of("hook", new PathItem().post(new Operation().tags(List.of("webhook")))))
                .components(new Components()
                        .addPathItem("reusable", new PathItem().get(new Operation().tags(List.of("componentsPathItem"))))
                        .addCallbacks("reusable", callbackItem("componentsCallback")))
                .tags(List.of(
                        new Tag().name("kept"),
                        new Tag().name("keptCallback"),
                        new Tag().name("removed"),
                        new Tag().name("removedCallback"),
                        new Tag().name("webhook"),
                        new Tag().name("componentsPathItem"),
                        new Tag().name("componentsCallback")));
    }

    private static Map<String, Callback> callback(String tag) {
        return Map.of("cb", callbackItem(tag));
    }

    private static Callback callbackItem(String tag) {
        return new Callback().addPathItem("{$request.body#/url}",
                new PathItem().post(new Operation().tags(List.of(tag))));
    }

    private static Set<String> tagNames(OpenAPI api) {
        return api.getTags() == null ? Set.of() : api.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
    }

    private static void customize(OpenAPI api, OpenApiModelRequestFilter filter) {
        new OpenApiModelFilterCustomizer(filter).customize(null, () -> api);
    }

    @Test
    public void tagsOutsideOfPaths() {

        OpenAPI api = apiWithTagsInEveryOperationHolder();
        customize(api, (r, p, m) -> "/kept".equals(p));

        assertEquals(Set.of(
                        "kept",
                        "keptCallback",
                        "webhook",
                        "componentsPathItem",
                        "componentsCallback"),
                tagNames(api),
                "Only the tags of the excluded path operation and its callback must be removed");
    }

    @Test
    public void tagsOutsideOfPaths_AllPathsExcluded() {

        OpenAPI api = apiWithTagsInEveryOperationHolder();
        customize(api, (r, p, m) -> false);

        assertEquals(Set.of("webhook", "componentsPathItem", "componentsCallback"), tagNames(api));
    }

    @Test
    public void tagsSharedWithOperationsOutsideOfPaths() {

        // the excluded "/removed" operation shares its tags with operations that are not reachable via "paths",
        // and hence are not filtered. Such tags are still in use and must be preserved

        Operation removed = new Operation().tags(List.of("webhook", "componentsPathItem", "componentsCallback", "nested"));

        Operation componentsPathItemOp = new Operation()
                .tags(List.of("componentsPathItem"))
                .callbacks(callback("nested"));

        OpenAPI api = new OpenAPI()
                .paths((Paths) new Paths().addPathItem("/removed", new PathItem().get(removed)))
                .webhooks(Map.of("hook", new PathItem().post(new Operation().tags(List.of("webhook")))))
                .components(new Components()
                        .addPathItem("reusable", new PathItem().get(componentsPathItemOp))
                        .addCallbacks("reusable", callbackItem("componentsCallback")))
                .tags(List.of(
                        new Tag().name("webhook"),
                        new Tag().name("componentsPathItem"),
                        new Tag().name("componentsCallback"),
                        new Tag().name("nested")));

        customize(api, (r, p, m) -> false);

        assertEquals(Set.of("webhook", "componentsPathItem", "componentsCallback", "nested"), tagNames(api));
    }

    @Test
    public void allTagsRemoved() {

        OpenAPI api = new OpenAPI()
                .paths((Paths) new Paths().addPathItem("/removed", new PathItem().get(new Operation().tags(List.of("removed")))))
                .tags(List.of(new Tag().name("removed")));

        customize(api, (r, p, m) -> false);
        assertNull(api.getTags(), "Empty 'tags' must not be preserved as an empty array");
    }

    @Test
    public void undeclaredTagsPreserved() {

        // a tag not attached to any operation is not something the filter has excluded, so it must be preserved
        OpenAPI api = new OpenAPI()
                .paths((Paths) new Paths().addPathItem("/removed", new PathItem().get(new Operation().tags(List.of("removed")))))
                .tags(List.of(new Tag().name("removed"), new Tag().name("standalone")));

        customize(api, (r, p, m) -> false);
        assertEquals(Set.of("standalone"), tagNames(api));
    }

    @Test
    public void circularCallbacks() {

        PathItem pi = new PathItem();
        Operation op = new Operation().tags(List.of("kept"));
        pi.setGet(op);

        // a callback pointing back at its own PathItem
        op.setCallbacks(Map.of("cb", (Callback) new Callback().addPathItem("self", pi)));

        OpenAPI api = new OpenAPI()
                .paths((Paths) new Paths().addPathItem("/kept", pi))
                .tags(List.of(new Tag().name("kept")));

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> customize(api, (r, p, m) -> true));
        assertEquals(Set.of("kept"), tagNames(api));
    }
}
