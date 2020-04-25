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
import io.bootique.jetty.servlet.StaticServlet;
import io.bootique.swagger.ui.model.SwaggerUIServletModel;
import io.bootique.swagger.ui.model.SwaggerUIServletTemplateModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @since 2.0
 */
// TODO: delegate to the StaticServlet instead of inheriting from it?
public class SwaggerUiServlet extends StaticServlet {

    static final String PATH_INFO_ONLY_PARAMETER = "pathInfoOnly";

    private Mustache template;
    private Map<String, SwaggerUIServletModel> models;

    public SwaggerUiServlet(String resourceBase, Mustache template, Map<String, SwaggerUIServletModel> models) {
        super(resourceBase);
        this.template = template;
        this.models = models;
    }

    @Override
    public String getInitParameter(String name) {

        // "pathInfoOnly = true" ensures that the part of the URL matching the servlet path
        // ("/swagger-ui" in our case) is not included in the file path when resolving a static resource.

        return PATH_INFO_ONLY_PARAMETER.equals(name) ? "true" : super.getInitParameter(name);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !"/".equals(pathInfo)) {
            super.doGet(request, response);
        } else {
            doGetConsole(request, response);
        }
    }

    protected void doGetConsole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        SwaggerUIServletModel model = models.get(servletPath);
        if (model == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        SwaggerUIServletTemplateModel templateModel = model.createTemplateModel(request);
        template.execute(response.getWriter(), templateModel).flush();
    }
}
