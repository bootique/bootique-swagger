package io.bootique.swagger.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.swagger.SwaggerModule;
import io.bootique.swagger.service.SwaggerService;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateSpecCommand extends CommandWithMetadata {

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
            var destinationDirectory = cli.optionString(SwaggerModule.DESTINATION_DIRECTORY_OPTION_NAME);
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
            var content = service.getOpenApiModel(key).get().render(key);
            Files.write(filePath, content.getBytes());
        }
    }

    private void printToStdOut() {
        var out = new StringBuilder();
        var service = serviceProvider.get();
        var keys = service.getUrlPatterns();
        keys.forEach(key -> {
            var content = service.getOpenApiModel(key).get().render(key);
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
                        "Use -d option to specify target directory.")
                .build();
    }
}
