package io.bootique.swagger.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerOpenapiApi;
import io.bootique.swagger.service.SwaggerService;

import javax.inject.Provider;

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
    return CommandOutcome.succeeded();
  }

  private String printModel(String key, OpenApiModel model) {
    if (key.endsWith("json")) {
      return SwaggerOpenapiApi.printJson(model);
    } else {
      return SwaggerOpenapiApi.printYaml(model);
    }
  }

  private static CommandMetadata createMetadata() {
    return CommandMetadata.builder(GenerateSpecCommand.class)
        .description("Generates openapi spec.")
        .build();
  }
}
