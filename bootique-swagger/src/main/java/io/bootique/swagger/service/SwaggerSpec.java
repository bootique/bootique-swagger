package io.bootique.swagger.service;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;

import java.util.List;

@BQConfig
public class SwaggerSpec {

  private String pathJson;
  private String pathYaml;
  private ResourceFactory spec;
  private ResourceFactory overrideSpec;
  private List<String> resourcePackages;
  private List<String> resourceClasses;

  public String getPathJson() {
    return pathJson;
  }

  public String getPathYaml() {
    return pathYaml;
  }

  public ResourceFactory getSpec() {
    return spec;
  }

  public ResourceFactory getOverrideSpec() {
    return overrideSpec;
  }

  public List<String> getResourcePackages() {
    return resourcePackages;
  }

  public List<String> getResourceClasses() {
    return resourceClasses;
  }

  @BQConfigProperty("An optional list of Java packages that contain annotated API endpoint classes")
  public void setResourcePackages(List<String> resourcePackages) {
    this.resourcePackages = resourcePackages;
  }

  @BQConfigProperty("An optional list of Java classes for the annotated API endpoints")
  public void setResourceClasses(List<String> resourceClasses) {
    this.resourceClasses = resourceClasses;
  }

  @BQConfigProperty("Publishes an OpenAPI metadata endpoint as JSON at the specified path")
  public void setPathJson(String pathJson) {
    this.pathJson = pathJson;
  }

  @BQConfigProperty("Publishes an OpenAPI metadata endpoint as YAML at the specified path")
  public void setPathYaml(String pathYaml) {
    this.pathYaml = pathYaml;
  }

  @BQConfigProperty("Location of the OpenAPI spec file. Overrides 'spec', 'resourcePackages', 'resourceClasses' models")
  public void setOverrideSpec(ResourceFactory overrideSpec) {
    this.overrideSpec = overrideSpec;
  }

  @BQConfigProperty("Location of the OpenAPI spec file. Overrides 'resourcePackages' and 'resourceClasses' model")
  public void setSpec(ResourceFactory spec) {
    this.spec = spec;
  }

}
