package com.revolut.accounts.webapp;

import java.net.URI;

public class Application {
    public static void main(String[] args) throws Exception {
        WebServer webServer = new WebServer(
                new URI("http://localhost:8080"),
                new ApplicationConfig()
        );
        webServer.start();
    }
}
