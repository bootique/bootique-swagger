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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;

/**
 * @since  1.0.RC1
 */
public class SwaggerUiMustacheServlet extends HttpServlet {

	private String url;
	private MustacheFactory mustacheFactory;

	public SwaggerUiMustacheServlet( String url) {
		this.url = url;
		this.mustacheFactory = new DefaultMustacheFactory();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		Model swaggerUiModel = new Model(url != null ? url: getBaseUrl(request) + "/swagger.json");
		Mustache mustache = compile();

		mustache.execute(out, swaggerUiModel).flush();
	}

	Mustache compile() {

		Reader reader = null;

		URL templateUrl = getClass().getClassLoader().getResource("swagger-ui/index.mustache");
		try {
			reader = new InputStreamReader(templateUrl.openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mustacheFactory.compile(reader, "index.mustache");
	}

	public static String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		String contextPath = request.getContextPath();

		String portExp = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)
				? ""
				: ":" + port;
		return scheme + "://" + host + portExp + contextPath;
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
