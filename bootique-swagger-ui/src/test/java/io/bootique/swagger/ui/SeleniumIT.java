package io.bootique.swagger.ui;

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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            .module(binder -> JerseyModule.extend(binder).addApiResource(TestApi.class))
            .createRuntime();

    @Options
    ChromeOptions chromeOptions = new ChromeOptions()
            .addArguments("--headless")
            .addArguments("--no-sandbox")
            .addArguments("--disable-dev-shm-usage")
            .addArguments("--remote-allow-origins=*");

    @Test
    public void api_Console(ChromeDriver driver) {
        driver.get(jetty.getUrl());
        assertEquals("{\"message\":\"hello test\"}", driver.findElement(By.tagName("pre")).getText());
    }

    @Disabled("Temporary disabled as unstable")
    @Test
    public void swaggerUIGet(ChromeDriver driver) {

        driver.get(jetty.getTarget().path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("div#operations-default-get.opblock.opblock-get")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.try-out__btn")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.execute.opblock-control__btn")));
        webElement.click();

        String curl = driver.findElement(By.cssSelector("textarea.curl")).getText();
        assertEquals(curl, "curl -X GET \"" + jetty.getUrl() + "/\" -H \"accept: application/json\"");

        String url = driver.findElement(By.cssSelector("div.request-url > pre")).getText();
        assertEquals(jetty.getUrl() + "/", url);

        String status = driver.findElement(By.cssSelector("tr.response > td.response-col_status")).getText();
        assertEquals(status, "200");

        String bodyMessage = driver.findElement(
                By.cssSelector("div.highlight-code:nth-child(2) > pre:nth-child(2) > span:nth-child(6)")).getText();
        assertEquals("\"hello test\"", bodyMessage);
    }

    @Test
    public void swaggerUIStatic() {
        Response r = jetty.getTarget().path("/swagger-ui/index.css").request().get();
        JettyTester.assertOk(r);
    }

    @Test
    public void openapiJson(ChromeDriver driver) {
        driver.get(jetty.getTarget().path("swagger-ui").getUri().toString());

        WebElement urlElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("span.url")));

        assertEquals(jetty.getUrl() + "/openapi.json", urlElement.getText());
    }

    // TODO: this initially only failed on Mac M1 due to the (unfixed) https://github.com/bonigarcia/selenium-jupiter/issues/238
    //   Now it fails in CI/CD as well (did GitHub switch to arm?)
    @Disabled
    @Test
    public void openapiYaml(ChromeDriver driver) {

        driver.get(jetty.getTarget().path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.tagName("input")));

        webElement.clear();
        webElement.sendKeys(jetty.getUrl() + "/openapi.yaml");

        driver.findElement(By.cssSelector("button.download-url-button.button")).click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("span.url")));

        assertEquals(jetty.getUrl() + "/openapi.yaml", webElement.getText());
    }

    @Disabled("Temporary disabled as unstable")
    @Test
    public void swaggerUIPost(ChromeDriver driver) {

        driver.get(jetty.getTarget().path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("div#operations-default-post.opblock.opblock-post")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.try-out__btn")));
        webElement.click();

        webElement = driver.findElement(By.cssSelector("textarea.body-param__text"));
        webElement.clear();

        webElement.sendKeys("{\"message\":\"hello test\"}");

        driver.findElement(By.cssSelector("button.btn.execute.opblock-control__btn")).click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(webDriver -> webDriver.findElement(By.cssSelector("textarea.curl")));

        String curl = webElement.getText();
        assertEquals("curl -X POST \"" + jetty.getUrl() + "/\" -H \"accept: application/json\" " +
                "-H \"Content-Type: */*\" -d \"{\\\"message\\\":\\\"hello test\\\"}\"", curl);

        String url = driver.findElement(By.cssSelector("div.request-url > pre")).getText();
        assertEquals(jetty.getUrl() + "/", url);

        String status = driver.findElement(By.cssSelector("div.responses-inner > div > div > table.responses-table > tbody > tr.response > td.response-col_status")).getText();
        assertEquals(status, "200");

        webElement = driver.findElement(
                By.cssSelector("div.highlight-code:nth-child(2) > pre:nth-child(2) > span:nth-child(6)"));
        assertEquals("\"hello test\"", webElement.getText());
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