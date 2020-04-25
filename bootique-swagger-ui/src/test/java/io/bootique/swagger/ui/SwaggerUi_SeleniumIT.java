package io.bootique.swagger.ui;

import io.bootique.BQCoreModule;
import io.bootique.jersey.JerseyModule;
import io.bootique.swagger.openapi.SwaggerOpenapiModuleProvider;
import io.bootique.test.junit5.BQTestClassFactory;
import io.github.bonigarcia.seljup.Options;
import io.github.bonigarcia.seljup.SeleniumExtension;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SeleniumExtension.class)
public class SwaggerUi_SeleniumIT {

    @Options
    ChromeOptions chromeOptions = new ChromeOptions();
    {
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
    }

    @RegisterExtension
    public static BQTestClassFactory TEST_FACTORY = new BQTestClassFactory();

    static WebTarget BASE_TARGET = ClientBuilder.newClient().target("http://127.0.0.1:8080/");

    @BeforeAll
    public static void beforeClass() {

        TEST_FACTORY.app("-s")
                .module(new SwaggerOpenapiModuleProvider())
                .module(new SwaggerUiModuleProvider())
                .module(binder -> BQCoreModule.extend(binder).addConfig("classpath:selenium/default.yml"))
                .module(binder -> JerseyModule.extend(binder).addResource(TestApi.class))
                .run();

    }

    @Test
    public void testApi_Console(ChromeDriver driver) {

        Response r = BASE_TARGET.request().get();
        assertEquals(200, r.getStatus());
        driver.get(BASE_TARGET.getUri().toString());
        assertEquals("TEST", driver.findElement(By.tagName("pre")).getText());

    }

    @Test
    public void testSwaggerUIGet(ChromeDriver driver) {

        Response r = BASE_TARGET.path("swagger-ui").request().get();
        assertEquals(200, r.getStatus());

        driver.get(BASE_TARGET.path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("div#operations-default-get.opblock.opblock-get")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.try-out__btn")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.execute.opblock-control__btn")));
        webElement.click();

        String curl = driver.findElement(By.cssSelector("textarea.curl")).getText();
        assertEquals(curl, "curl -X GET \"http://127.0.0.1:8080/\" -H \"accept: application/json\"");

        String url = driver.findElement(By.cssSelector("div.request-url > pre")).getText();
        assertEquals(url, "http://127.0.0.1:8080/");

        String status = driver.findElement(By.cssSelector("tr.response > td.response-col_status")).getText();
        assertEquals(status, "200");

        List<WebElement> spanList = driver.findElements(By.cssSelector("div.responses-inner > div > div > table > tbody > tr.response > td.col.response-col_description > div > div.highlight-code > pre.microlight > span"));
        String response = "";
        for (WebElement span : spanList) {
            response += span.getText();
        }
        assertEquals(response,
                "can't parse JSON.  Raw result:\n" +
                        "\n" +
                        "TEST");

    }

    @Test
    public void testSwaggerUIStatic() {
        Response r = BASE_TARGET.path("/swagger-ui/static/").request().get();
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testOpenapiJson(ChromeDriver driver) {

        driver.get(BASE_TARGET.path("openapi.json").getUri().toString());
        String homeUrl = driver.findElement(By.tagName("pre")).getText();
        assertEqualsToResourceContents("selenium/openapi-test.json", homeUrl);

    }

    @Test
    public void testOpenapiYaml(ChromeDriver driver) {

        driver.get(BASE_TARGET.path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.tagName("input")));

        webElement.clear();
        webElement.sendKeys("http://127.0.0.1:8080/openapi.yaml");

        driver.findElement(By.cssSelector("button.download-url-button.button")).click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("span.url")));

        assertEquals(webElement.getText(), "http://127.0.0.1:8080/openapi.yaml");

    }

    @Test
    public void testSwaggerUIPost(ChromeDriver driver) {

        driver.get(BASE_TARGET.path("swagger-ui").getUri().toString());

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("div#operations-default-post.opblock.opblock-post")));
        webElement.click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(3).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("button.btn.try-out__btn")));
        webElement.click();

        webElement = driver.findElement(By.cssSelector("textarea.body-param__text"));
        webElement.clear();

        String stringTest = "\"TEST\"";
        webElement.sendKeys(stringTest);

        driver.findElement(By.cssSelector("button.btn.execute.opblock-control__btn")).click();

        webElement = new WebDriverWait(driver, Duration.ofSeconds(1).getSeconds())
                .until(webDriver -> webDriver.findElement(By.cssSelector("textarea.curl")));

        String curl = webElement.getText();
        assertEquals(curl, "curl -X POST \"http://127.0.0.1:8080/\" -H \"accept: application/json\" -H \"Content-Type: */*\" -d \"\\\"TEST\\\"\"");

        String url = driver.findElement(By.cssSelector("div.request-url > pre")).getText();
        assertEquals(url, "http://127.0.0.1:8080/");

        String status = driver.findElement(By.cssSelector("div.responses-inner > div > div > table.responses-table > tbody > tr.response > td.response-col_status")).getText();
        assertEquals(status, "200");

        webElement = driver.findElement(By.cssSelector("div.responses-inner > div > div > table.responses-table > tbody > tr.response > td.col.response-col_description > div > div.highlight-code > pre.microlight > span"));
        assertEquals(webElement.getText(), stringTest);

    }

    void assertEqualsToResourceContents(String expectedResource, String toTest) {

        ClassLoader cl = getClass().getClassLoader();

        try (InputStream in = cl.getResourceAsStream(expectedResource)) {
            assertNotNull(in);

            // read as bytes to preserve line breaks
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                out.write(data, 0, nRead);
            }

            String expectedString = new String(out.toByteArray(), "UTF-8");
            assertEquals(expectedString, toTest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OpenAPIDefinition
    @Path("/")
    public static class TestApi {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(description = "Returns hello message")
        public String get() {
            return "TEST";
        }

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(description = "Returns hello message")
        public String post(String request) {
            return request;
        }

    }
}