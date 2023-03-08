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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.bootique.jetty.MappedServlet;
import io.bootique.swagger.ui.model.SwaggerUIServletModel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**
 * @since 2.0
 */
// notice that this is not a BQConfig factory, and it is created manually,
// as we'd like to avoid "modelFactories" property exposure in YAML
// TODO: some Jackson trick to deserialize this as a map?
public class SwaggerUiServletFactory {

    private Map<String, SwaggerUIModelFactory> modelFactories;

    public SwaggerUiServletFactory(Map<String, SwaggerUIModelFactory> modelFactories) {
        this.modelFactories = modelFactories;
    }

    public MappedServlet<SwaggerUiServlet> createServlet() {

        Map<String, SwaggerUIServletModel> models = new HashMap<>();
        modelFactories.values()
                .stream()
                .map(SwaggerUIModelFactory::createModel)
                .flatMap(Optional::stream)
                .forEach((m -> models.put(m.getUiPath(), m)));

        Set<String> urlPatterns = new HashSet<>();
        models.values().stream().map(SwaggerUIServletModel::getUrlPattern).forEach(urlPatterns::add);

        String swaggerUiVersion = readSwaggerUiVersion();
        String resourceBase = swaggerUiResourceBase(swaggerUiVersion);

        SwaggerUiServlet servlet = new SwaggerUiServlet(resourceBase, compileTemplate(), models);
        return new MappedServlet<>(servlet, urlPatterns, "swagger-ui");
    }

    protected String swaggerUiResourceBase(String swaggerUiVersion) {
        String path = "META-INF/resources/webjars/swagger-ui/" + swaggerUiVersion;
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new RuntimeException("Swagger UI properties resource location does not exist: '" + path + "'");
        }

        return url.toString();
    }

    protected String readSwaggerUiVersion() {
        String propsPath = "META-INF/maven/org.webjars/swagger-ui/pom.properties";
        URL swaggerUIProps = getClass().getClassLoader().getResource(propsPath);
        if (swaggerUIProps == null) {
            throw new RuntimeException("Swagger UI properties file is not found at '" + propsPath + "'");
        }

        Properties props = new Properties();
        try (Reader in = new InputStreamReader(swaggerUIProps.openStream())) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Error reading Swagger UI properties file at '" + propsPath + "'", e);
        }

        String version = props.getProperty("version");
        if (version == null) {
            throw new RuntimeException("Error reading Swagger UI properties file at '" + propsPath + "': no 'version' property present");
        }

        return version;
    }

    protected Mustache compileTemplate() {
        URL templateUrl = getClass().getClassLoader().getResource("io/bootique/swagger/ui/mustache/index.mustache");
        try (Reader reader = new InputStreamReader(templateUrl.openStream())) {
            return new DefaultMustacheFactory().compile(reader, "index.mustache");
        } catch (IOException e) {
            throw new RuntimeException("Error reading Mustache template " + templateUrl, e);
        }
    }
}
