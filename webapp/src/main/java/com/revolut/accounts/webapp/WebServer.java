package com.revolut.accounts.webapp;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;

import java.net.URI;

public class WebServer {
    private final Server jettyServer;

    public WebServer(URI baseUri) {
        jettyServer = JettyHttpContainerFactory.createServer(
                baseUri, new ApplicationConfig(), false
        );
    }

    public void start() throws Exception {
        jettyServer.start();
    }

    public void stop() throws Exception {
        jettyServer.stop();
        jettyServer.join();
        jettyServer.destroy();
    }
}
