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
package io.bootique.swagger.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.swagger.SwaggerService;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateSpecCommand extends CommandWithMetadata {

    private static final String DESTINATION_DIRECTORY_OPTION_NAME = "destDir";

    private final BootLogger bootLogger;
    private final Provider<SwaggerService> serviceProvider;

    public GenerateSpecCommand(BootLogger bootLogger, Provider<SwaggerService> serviceProvider) {
        super(createMetadata());
        this.bootLogger = bootLogger;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        try {
            var destinationDirectory = cli.optionString(DESTINATION_DIRECTORY_OPTION_NAME);
            if (destinationDirectory == null) {
                printToStdOut();
            } else {
                saveToFiles(destinationDirectory);
            }
            return CommandOutcome.succeeded();
        } catch (IOException e) {
            return CommandOutcome.failed(1, e);
        }
    }

    private void saveToFiles(String destinationDirectory) throws IOException {
        var service = serviceProvider.get();
        var keys = service.getUrlPatterns();
        var destinationDirPath = Paths.get(destinationDirectory);
        if (Files.notExists(destinationDirPath)) {
            Files.createDirectories(destinationDirPath);
        }
        for (String key : keys) {
            var fileName = key.replace("/", "_");
            var filePath = destinationDirPath.resolve(fileName);
            var content = service.getOpenApiModel(key).render(key);
            Files.write(filePath, content.getBytes());
        }
    }

    private void printToStdOut() {
        var out = new StringBuilder();
        var service = serviceProvider.get();
        var keys = service.getUrlPatterns();
        keys.forEach(key -> {
            var content = service.getOpenApiModel(key).render(key);
            out
                    .append(key)
                    .append("\n")
                    .append(content)
                    .append("\n\n");
        });
        bootLogger.stdout(out.toString());
    }

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(GenerateSpecCommand.class)
                .description("Generates openapi spec. By default prints spec contents to std out. " +
                        "To generate spec files use -d option and specify target directory.")
                .addOption(destinationDirectoryOption())
                .build();
    }

    private static OptionMetadata destinationDirectoryOption() {
        return OptionMetadata.builder(DESTINATION_DIRECTORY_OPTION_NAME, "Directory to save swagger spec files.")
                .shortName('d')
                .valueOptional()
                .build();
    }
}
