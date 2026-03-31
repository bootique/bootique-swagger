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
package io.bootique.swagger;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jetty.junit.JettyTester;
import io.bootique.junit.BQApp;
import io.bootique.junit.BQTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
public class ConflictingSchemaIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-s", "-c", "classpath:config11/startup.yml")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .createRuntime();

    @Test
    public void conflictingSchemaWarn() {
        PrintStream savedErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));

        try {
            Response r = jetty.getTarget().path("/model.json").request().get();
            JettyTester.assertOk(r);
        } finally {
            System.setErr(savedErr);
        }

        String log = captured.toString();
        assertTrue(log.contains("** Multiple classes are mapped to OpenAPI schema 'MySchema'"), log);
        assertTrue(log.contains("io.bootique.swagger.config11.p1.MySchema"), "Expected p1.MySchema in warning");
        assertTrue(log.contains("io.bootique.swagger.config11.p2.MySchema"), "Expected p2.MySchema in warning");
    }
}
