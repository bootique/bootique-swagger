package io.bootique.swagger.redoc;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.github.bonigarcia.seljup.Options;
import io.github.bonigarcia.seljup.SeleniumJupiter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@BQTest
@ExtendWith(SeleniumJupiter.class)
public class SeleniumIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-s")
            .autoLoadModules()
            .module(jetty.moduleReplacingConnectors())
            .module(binder -> BQCoreModule.extend(binder).addConfig("classpath:SeleniumIT/startup.yml"))
            .module(binder -> JerseyModule.extend(binder).addResource(TestApi.class))
            .createRuntime();

    @Options
    ChromeOptions chromeOptions = new ChromeOptions()
            .addArguments("--headless")
            .addArguments("--no-sandbox")
            .addArguments("--disable-dev-shm-usage")
            .addArguments("--remote-allow-origins=*");

    @Test
    public void testApi_Console(ChromeDriver driver) {
        driver.get(jetty.getUrl());
        assertEquals("{\"message\":\"hello test\"}", driver.findElement(By.tagName("pre")).getText());
    }

    @Test
    public void testOpenapiJson(ChromeDriver driver) {
        driver.get(jetty.getTarget().path("redoc").getUri().toString());

        WebElement urlElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.xpath("//a[text()='Download']")));

        assertEquals(jetty.getUrl() + "/openapi.json", urlElement.getAttribute("href"));
    }

    // TODO: this fails on Mac M1. Try again when https://github.com/bonigarcia/selenium-jupiter/issues/238 is fixed
    @DisabledOnOs(architectures = "aarch64", value = OS.MAC)
    @Test
    public void testOpenapiYaml(ChromeDriver driver) {

        driver.get(jetty.getTarget().path("redoc").getUri().toString());

        WebElement inputElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.tagName("input")));

        inputElement.clear();
        inputElement.sendKeys(jetty.getUrl() + "/openapi.yaml");

        driver.findElement(By.cssSelector("button.download-url-button.button")).click();

        WebElement urlElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("span.url")));

        assertEquals(jetty.getUrl() + "/openapi.yaml", urlElement.getText());
    }


    @OpenAPIDefinition
    @Path("/")
    public static class TestApi {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(description = "Returns hello message")
        public String get() {
            return "{\"message\":\"hello test\"}";
        }

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(description = "Returns hello message")
        public String post(String request) {
            return request;
        }

    }
}