package io.bootique.swagger;

import io.bootique.jersey.JerseyModule;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.swagger.config3.Api31;
import io.bootique.swagger.service.SwaggerService;

import org.junit.jupiter.api.Test;

@BQTest
public class GenerateSpecCommand_IT {

  @BQTestTool
  final BQTestFactory testFactory = new BQTestFactory();

  @Test
  void test() {
    var result = testFactory.app("--generate-spec"
            , "--config=classpath:config3/startup1.yml"
        )
        .autoLoadModules()
        .module(b -> JerseyModule.extend(b).addPackage(Api31.class));

    var swaggerService = result.createRuntime().getInstance(SwaggerService.class);
  }

  @Test
  void runCommand() {
    var result = testFactory.app("--generate-spec"
            , "--config=classpath:config3/startup1.yml"
        )
        .autoLoadModules()
        .module(b -> JerseyModule.extend(b).addPackage(Api31.class))
        .run();
  }

  @Test
  void runCommandWithManyPaths() {
    var result = testFactory.app("--generate-spec"
            , "--config=classpath:config2/startup.yml"
        )
        .autoLoadModules()
        .module(b -> JerseyModule.extend(b).addPackage(Api31.class))
        .run();
  }
}
