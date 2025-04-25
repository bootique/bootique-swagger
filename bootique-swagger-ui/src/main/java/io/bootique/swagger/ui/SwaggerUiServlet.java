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

import com.github.mustachejava.Mustache;
import io.bootique.swagger.ui.model.SwaggerUIServletModel;
import io.bootique.swagger.ui.model.SwaggerUIServletTemplateModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.DefaultServlet;

import java.io.IOException;
import java.util.Map;

/**
 * A servlet that renders dynamically-generated Swagger console HTML for the root path, and for all other paths
 * serves Swagger static resources (JS, CSS, etc.).
 *
 * @since 2.0
 */
public class SwaggerUiServlet extends DefaultServlet {

    static final String PATH_INFO_ONLY_PARAMETER = "pathInfoOnly";
    static final String RESOURCE_BASE_PARAMETER = "resourceBase";

    private final String resourceBase;
    private final Mustache template;
    private final Map<String, SwaggerUIServletModel> models;

    public SwaggerUiServlet(String resourceBase, Mustache template, Map<String, SwaggerUIServletModel> models) {
        this.resourceBase = resourceBase;
        this.template = template;
        this.models = models;
    }

    @Override
    public String getInitParameter(String name) {

        // special rules for Bootique-defined parameters
        switch (name) {
            // "pathInfoOnly = true" ensures that the part of the URL matching the servlet path
            // ("/swagger-ui" in our case) is not included in the file path when resolving a static resource.
            case PATH_INFO_ONLY_PARAMETER:
                return "true";
            case RESOURCE_BASE_PARAMETER:
                return this.resourceBase;
            default:
                return super.getInitParameter(name);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String pathInfo = request.getPathInfo();

        // root path - generate console HTML
        if (pathInfo == null || pathInfo.equals("/")) {
            doGetConsole(request, response);
        }
        // anything else is a static resource
        else {
            super.doGet(request, response);
        }
    }

    protected void doGetConsole(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String servletPath = request.getServletPath();
        SwaggerUIServletModel model = models.get(servletPath);
        if (model == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("text/html");

        SwaggerUIServletTemplateModel templateModel = model.createTemplateModel(request);
        template.execute(response.getWriter(), templateModel).flush();
    }
}
