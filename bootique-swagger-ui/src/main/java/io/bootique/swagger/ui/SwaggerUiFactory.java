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
import io.bootique.jetty.MappedServlet;
import io.bootique.swagger.ui.mustache.SwaggerUiServlet;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * @since 1.0.RC1
 */
@BQConfig
public class SwaggerUiFactory {

    private String specUrl;
    private String specPath;
    private String urlPattern;

    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        String portExp = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)
                ? ""
                : ":" + port;
        return scheme + "://" + host + portExp + contextPath;
    }

    @BQConfigProperty("A full URL of the JSON/YAML descriptor resource")
    public void setSpecUrl(String specUrl) {
        this.specUrl = specUrl;
    }

    /**
     * @since 2.0
     */
    @BQConfigProperty
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public MappedServlet<SwaggerUiServlet> createJerseyServlet() {
        SwaggerUiServlet servlet = new SwaggerUiServlet(specUrlResolver());
        return new MappedServlet<>(servlet, urlPatterns(), "swagger-ui");
    }

    private Function<HttpServletRequest, String> specUrlResolver() {

        // use full URL
        if (specUrl != null) {
            return r -> specUrl;
        }

        // resolve relative to the current app context
        String specPath = getSpecPath();
        return r -> getBaseUrl(r) + specPath;
    }

    private String getSpecPath() {
        if (specPath == null) {
            return "/swagger.json";
        }

        if (specPath.startsWith("/")) {
            return specPath;
        }

        return "/" + specPath;
    }

    /**
     * @since 2.0
     */
    @BQConfigProperty("A path of the JSON/YAML descriptor resource relative to this app context. Ignored if 'specUrl' is set.")
    public void setSpecPath(String specPath) {
        this.specPath = specPath;
    }

    private Set<String> urlPatterns() {
        String pattern = this.urlPattern != null ? this.urlPattern : "/swagger-ui";
        return Collections.singleton(pattern);
    }
}
