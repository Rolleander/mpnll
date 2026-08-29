package com.broll.mpnll.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.broll.mpnll.server.MpnllServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class GwtSmokeTest {

    private MpnllServer server;
    private HttpServer webServer;
    private WebDriver browser;

    private static void serveFile(Path root, HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.endsWith("/")) {
            requestPath += "index.html";
        }
        Path file = root.resolve(requestPath.substring(1)).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        byte[] content = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file));
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(content);
        }
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    @Before
    public void startServers() throws Exception {
        server = new MpnllServer();
        server.setName("GWT smoke server");
        server.open(0, 0);
        server.getLobbyHandler().openLobby(lobby -> lobby.setName("Browser lobby"));

        Path smokeWar = Paths.get(System.getProperty("gwtSmokeWar"));
        webServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        webServer.createContext("/", exchange -> serveFile(smokeWar, exchange));
        webServer.start();
    }

    @After
    public void stopServers() {
        if (browser != null) {
            browser.quit();
        }
        if (webServer != null) {
            webServer.stop(0);
        }
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void gwtClientListsLobbies() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        browser = new ChromeDriver(options);

        String smokeUrl = "http://127.0.0.1:" + webServer.getAddress().getPort()
            + "/smoke/?wsPort=" + server.getWebsocketPort();
        browser.get(smokeUrl);

        WebDriverWait wait = new WebDriverWait(browser, Duration.ofSeconds(15));
        WebElement status = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("sanity-status"))
        );
        wait.until(driver -> {
            String state = status.getAttribute("data-state");
            return "PASS".equals(state) || "FAIL".equals(state);
        });

        String events = browser.findElement(By.id("sanity-events")).getAttribute("value");
        assertEquals(events, "PASS", status.getAttribute("data-state"));
        assertTrue(events, events.contains("listed 1 lobbies"));
    }
}
