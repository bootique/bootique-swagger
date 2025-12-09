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
package io.bootique.swagger.ui.model;

import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Function;

/**
 * @since 2.0
 */
public class SwaggerUIServletModel {

    private final Function<HttpServletRequest, String> specUrlResolver;
    private final String uiPath;
    private final String requestInterceptor;

    public SwaggerUIServletModel(Function<HttpServletRequest, String> specUrlResolver, String uiPath, String requestInterceptor) {
        this.specUrlResolver = specUrlResolver;
        this.uiPath = uiPath;
        this.requestInterceptor = requestInterceptor;
    }

    public SwaggerUIServletTemplateModel createTemplateModel(HttpServletRequest request) {
        String apiUrl = getSpecUrl(request);
        String resourcePath = getResourcePath(request);
        return new SwaggerUIServletTemplateModel(apiUrl, resourcePath, requestInterceptor);
    }

    public String getUiPath() {
        return uiPath;
    }

    public String getUrlPattern() {
        return uiPath + "/*";
    }

    private String getSpecUrl(HttpServletRequest request) {
        return specUrlResolver.apply(request);
    }

    private String getResourcePath(HttpServletRequest request) {
        String context = request.getContextPath();
        return context + uiPath;
    }
}
