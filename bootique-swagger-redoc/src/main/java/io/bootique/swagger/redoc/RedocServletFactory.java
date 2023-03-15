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

package io.bootique.swagger.redoc;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.bootique.jetty.MappedServlet;
import io.bootique.swagger.redoc.model.RedocServletModel;

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
// TODO: some Jackson trick to derserialize this as a map?
public class RedocServletFactory {

    private Map<String, RedocModelFactory> modelFactories;

    public RedocServletFactory(Map<String, RedocModelFactory> modelFactories) {
        this.modelFactories = modelFactories;
    }

    public MappedServlet<RedocServlet> createServlet() {

        Map<String, RedocServletModel> models = new HashMap<>();
        modelFactories.values()
                .stream()
                .map(RedocModelFactory::createModel)
                .forEach((m -> models.put(m.getUiPath(), m)));

        Set<String> urlPatterns = new HashSet<>();
        models.values().stream().map(RedocServletModel::getUrlPattern).forEach(urlPatterns::add);

        String redociVersion = readRedocVersion();
        String resourceBase = redocResourceBase(redociVersion);

        RedocServlet servlet = new RedocServlet(resourceBase, compileTemplate(), models);
        return new MappedServlet<>(servlet, urlPatterns, "redoc");
    }

    protected String redocResourceBase(String redocVersion) {
        String path = "META-INF/resources/webjars/redoc/" + redocVersion;
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new RuntimeException("Redoc properties resource location does not exist: '" + path + "'");
        }

        return url.toString();
    }

    protected String readRedocVersion() {
        String propsPath = "META-INF/maven/org.webjars/redoc/pom.properties";
        URL redocProps = getClass().getClassLoader().getResource(propsPath);
        if (redocProps == null) {
            throw new RuntimeException("Redoc properties file is not found at '" + propsPath + "'");
        }

        Properties props = new Properties();
        try (Reader in = new InputStreamReader(redocProps.openStream())) {
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
        URL templateUrl = getClass().getClassLoader().getResource("io/bootique/swagger/redoc/mustache/index.mustache");
        try (Reader reader = new InputStreamReader(templateUrl.openStream())) {
            return new DefaultMustacheFactory().compile(reader, "index.mustache");
        } catch (IOException e) {
            throw new RuntimeException("Error reading Mustache template " + templateUrl, e);
        }
    }
}
