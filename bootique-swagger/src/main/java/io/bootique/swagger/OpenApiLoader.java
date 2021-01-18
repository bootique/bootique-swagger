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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @since 2.0
 */
public class OpenApiLoader {

    private Application application;

    public OpenApiLoader(Application application) {
        this.application = application;
    }

    public OpenAPI load(List<String> resourcePackages, List<String> resourceClasses, URL specLocation, URL overrideSpecLocation) {

        // override order
        // 1. class annotations
        // 2. spec (overrides annotations)
        // 3. override spec (overrides spec and annotations)

        OpenAPI empty = new OpenAPI();

        OpenAPI specFromAnnotations = !resourcePackages.isEmpty() || !resourceClasses.isEmpty()
                ? loadSpecFromAnnotations(empty, resourcePackages, resourceClasses) : empty;
        OpenAPI spec = specLocation != null ? merge(specFromAnnotations, loadSpec(specLocation)) : specFromAnnotations;
        OpenAPI specOverride = overrideSpecLocation != null ? merge(spec, loadSpec(overrideSpecLocation)) : spec;

        // sort paths for stable specs... OpenAPI loads them in different order depending on the JVM version
        return sortPaths(specOverride);
    }

    protected OpenAPI loadSpecFromAnnotations(OpenAPI mergeInto, List<String> resourcePackages, List<String> resourceClasses) {

        SwaggerConfiguration config = new SwaggerConfiguration();
        config.setOpenAPI(mergeInto);

        if (!resourcePackages.isEmpty()) {
            config.setResourcePackages(new HashSet<>(resourcePackages));
        }

        if (!resourceClasses.isEmpty()) {
            config.setResourceClasses(new HashSet<>(resourceClasses));
        }

        JaxrsAnnotationScanner scanner = new JaxrsAnnotationScanner();
        scanner.setConfiguration(config);
        scanner.setApplication(application);

        Reader reader = new Reader();
        reader.setConfiguration(config);
        reader.setApplication(application);
        return reader.read(scanner.classes(), scanner.resources());
    }

    protected OpenAPI sortPaths(OpenAPI api) {

        Paths paths = api.getPaths();
        if (paths == null) {
            return api;
        }

        String[] keys = paths.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        Paths sorted = new Paths();
        for (String key : keys) {
            sorted.put(key, paths.get(key));
        }

        api.setPaths(sorted);

        return api;
    }

    protected OpenAPI loadSpec(URL location) {

        ObjectMapper mapper = createMapper(location);
        try {
            return mapper.readValue(location, OpenAPI.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration from " + location, e);
        }
    }

    protected ObjectMapper createMapper(URL location) {
        return location.getFile().toLowerCase().endsWith(".json")
                ? Json.mapper()
                : Yaml.mapper();
    }

    protected OpenAPI merge(OpenAPI mergeInto, OpenAPI toMerge) {

        // Jackson default merging algorithm ("mapper.readerForUpdating(mergeInto).readValue(location)") will only do
        // a shallow merge, hence overriding complex child structures like Components. So we have to do the merging
        // manually...

        // The unfortunate effect of this is manual maintenance: we will need to follow any future structure changes
        // in OpenAPI and merge them


        // The trick in mergin OpenAPI and each of its components is to determine what should be shallow-merged, and
        // what deep-merged. E.g. the goal may not to merge properties from different sources into a given Schema.
        // (Meaning each Schema should be atomically defined by whatever source provides it). But Schemas coming from
        // different sources should all be combined.

        // TODO: chances are we won't get the above right from the first try. May have to replace some of the currently
        //   "shallow" merges with "deep" merging if there are realistic use cases for them.

        // .. merged
        if (toMerge.getComponents() != null) {
            mergeInto.setComponents(mergeComponents(mergeInto.getComponents(), toMerge.getComponents()));
        }

        // .. merged
        if (toMerge.getExtensions() != null) {
            toMerge.getExtensions().forEach(mergeInto::addExtension);
        }

        // .. merged
        if (toMerge.getPaths() != null) {
            mergeInto.setPaths(mergePaths(mergeInto.getPaths(), toMerge.getPaths()));
        }

        // .. replaced
        if (toMerge.getOpenapi() != null) {
            mergeInto.setOpenapi(toMerge.getOpenapi());
        }

        // .. replaced
        if (toMerge.getExternalDocs() != null) {
            mergeInto.setExternalDocs(toMerge.getExternalDocs());
        }

        // .. replaced
        if (toMerge.getInfo() != null) {
            mergeInto.setInfo(toMerge.getInfo());
        }

        // .. replaced (TODO: merge?)
        if (toMerge.getSecurity() != null) {
            mergeInto.setSecurity(toMerge.getSecurity());
        }

        // .. replaced (TODO: merge?)
        if (toMerge.getServers() != null) {
            mergeInto.setServers(toMerge.getServers());
        }

        // .. replaced (TODO: merge?)
        if (toMerge.getTags() != null) {
            mergeInto.setTags(toMerge.getTags());
        }

        return mergeInto;
    }

    protected Components mergeComponents(Components mergeInto, Components toMerge) {

        Components merged = mergeInto != null ? mergeInto : new Components();

        if (toMerge.getCallbacks() != null) {
            toMerge.getCallbacks().forEach(merged::addCallbacks);
        }

        if (toMerge.getExamples() != null) {
            toMerge.getExamples().forEach(merged::addExamples);
        }

        if (toMerge.getExtensions() != null) {
            toMerge.getExtensions().forEach(merged::addExtension);
        }

        if (toMerge.getHeaders() != null) {
            toMerge.getHeaders().forEach(merged::addHeaders);
        }

        if (toMerge.getLinks() != null) {
            toMerge.getLinks().forEach(merged::addLinks);
        }

        if (toMerge.getParameters() != null) {
            toMerge.getParameters().forEach(merged::addParameters);
        }

        if (toMerge.getRequestBodies() != null) {
            toMerge.getRequestBodies().forEach(merged::addRequestBodies);
        }

        if (toMerge.getResponses() != null) {
            toMerge.getResponses().forEach(merged::addResponses);
        }

        if (toMerge.getSchemas() != null) {
            toMerge.getSchemas().forEach(merged::addSchemas);
        }

        if (toMerge.getSecuritySchemes() != null) {
            toMerge.getSecuritySchemes().forEach(merged::addSecuritySchemes);
        }

        return merged;
    }

    protected Paths mergePaths(Paths mergeInto, Paths toMerge) {
        Paths merged = mergeInto != null ? mergeInto : new Paths();

        merged.putAll(toMerge);

        if (toMerge.getExtensions() != null) {
            toMerge.getExtensions().forEach(merged::addExtension);
        }

        return merged;
    }
}
