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
package io.bootique.swagger.jakarta;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

/**
 * @since 2.0.B1
 */
public class OpenApiMerger {

    public static OpenAPI merge(OpenAPI mergeInto, OpenAPI toMerge) {

        // Jackson default merging algorithm ("mapper.readerForUpdating(mergeInto).readValue(location)") will only do
        // a shallow merge, overriding and destroying complex structures like Components. So we have to do merging
        // manually, deciding whether a shallow or a deep merge is appropriate at each level.

        // E.g. Components are using deep merge. Schemas are using shallow merge, meaning each of them should be
        // atomically defined by whatever source provides them.

        // TODO: chances are we won't get the "shallow vs. deep" right from the first try. May have to turn some of the
        //  presently "shallow" structures into "deep" as realistic use cases emerge. Or maybe it should be universally
        //  "deep" for each spec object?

        // TODO: another unfortunate effect of this implementation is a need for manual maintenance: we will have to
        //  track any future structure changes in OpenAPI and add them to the merge algorithm.

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

    protected static Components mergeComponents(Components mergeInto, Components toMerge) {

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

    protected static Paths mergePaths(Paths mergeInto, Paths toMerge) {
        Paths merged = mergeInto != null ? mergeInto : new Paths();

        merged.putAll(toMerge);

        if (toMerge.getExtensions() != null) {
            toMerge.getExtensions().forEach(merged::addExtension);
        }

        return merged;
    }
}
