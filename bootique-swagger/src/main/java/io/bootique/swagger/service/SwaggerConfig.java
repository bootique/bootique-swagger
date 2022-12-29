package io.bootique.swagger.service;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import java.util.Map;

@BQConfig
public class SwaggerConfig {
  private ResourceFactory overrideSpec;
  private Map<String, SwaggerSpec> specs;
  private boolean pretty = true;

  public ResourceFactory getOverrideSpec() {
    return overrideSpec;
  }

  public Map<String, SwaggerSpec> getSpecs() {
    return specs;
  }

  public boolean isPretty() {
    return pretty;
  }

  @BQConfigProperty("Zero or more API specifications provided by the application")
  public void setSpecs(Map<String, SwaggerSpec> specs) {
    this.specs = specs;
  }

  @BQConfigProperty("Location of the OpenAPI spec file, that overrides 'spec', 'resourcePackages', 'resourceClasses' models. " +
      "This setting is shared by all child specs, unless they define an explicit 'overrideSpec' of their own")
  public void setOverrideSpec(ResourceFactory overrideSpec) {
    this.overrideSpec = overrideSpec;
  }

  @BQConfigProperty("Whether to format YAML and JSON. 'True' by default. This setting is shared by all child specs.")
  public void setPretty(boolean pretty) {
    this.pretty = pretty;
  }
}
