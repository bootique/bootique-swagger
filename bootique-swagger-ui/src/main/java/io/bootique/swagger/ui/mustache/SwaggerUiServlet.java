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

package io.bootique.swagger.ui.mustache;

import com.github.mustachejava.Mustache;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

/**
 * @since 2.0
 */
public class SwaggerUiServlet extends HttpServlet {

    private Function<HttpServletRequest, String> specUrlResolver;
    private Mustache template;

    public SwaggerUiServlet(Mustache template, Function<HttpServletRequest, String> specUrlResolver) {
        this.template = template;
        this.specUrlResolver = specUrlResolver;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = specUrlResolver.apply(request);
        Model swaggerUiModel = new Model(url);
        template.execute(response.getWriter(), swaggerUiModel).flush();
    }

    private static class Model {

        private String url;

        public Model(String url) {
            this.url = url;
        }

        String getUrl() {
            return url;
        }
    }
}
