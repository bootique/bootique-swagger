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
package io.bootique.swagger.ui;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.swagger.ui.model.SwaggerUIServletModel;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * @since 2.0
 */
@BQConfig("A Swagger UI 'console' pointing to a specific API model")
public class SwaggerUIModelFactory {

    private String specUrl;
    private String specPath;
    private String uiPath;

    private static String getBaseUrl(HttpServletRequest request) {

        // this scheme works when Jetty is accessed directly. When running behind the proxy, it requires the proxy
        // to pass a "Host:" header with the public host[:port] name..

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        String portExp = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)
                ? ""
                : ":" + port;
        return scheme + "://" + host + portExp + contextPath;
    }

    public SwaggerUIServletModel createModel() {
        return new SwaggerUIServletModel(specUrlResolver(), uiPath());
    }

    @BQConfigProperty("A full URL of the JSON/YAML descriptor resource")
    public void setSpecUrl(String specUrl) {
        this.specUrl = specUrl;
    }

    @BQConfigProperty("A servlet path without wildcards corresponding to the UI console for the specified model")
    public void setUiPath(String uiPath) {
        this.uiPath = uiPath;
    }

    @BQConfigProperty("A path of the JSON/YAML descriptor resource relative to this app context. Ignored if 'specUrl' is set.")
    public void setSpecPath(String specPath) {
        this.specPath = specPath;
    }

    private String uiPath() {
        return uiPath != null ? uiPath : "/swagger-ui";
    }

    private Function<HttpServletRequest, String> specUrlResolver() {
        return specUrl != null ? r -> specUrl : specPathResolver();
    }

    private Function<HttpServletRequest, String> specPathResolver() {

        if(specPath == null) {
            return r -> null;
        }

        // resolve relative to the current app context
        String specPath = resolveSpecPath();
        return r -> getBaseUrl(r) + specPath;
    }

    private String resolveSpecPath() {

        if (specPath.startsWith("/")) {
            return specPath;
        }

        return "/" + specPath;
    }
}
