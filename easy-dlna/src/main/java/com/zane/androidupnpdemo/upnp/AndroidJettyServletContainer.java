/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zane.androidupnpdemo.upnp;

import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/27 17:20
 */

public class AndroidJettyServletContainer implements ServletContainerAdapter {
    final private static Logger log = Logger.getLogger(AndroidJettyServletContainer.class.getName());

    // Singleton
    public static final AndroidJettyServletContainer INSTANCE = new AndroidJettyServletContainer();

    private AndroidJettyServletContainer() {
        resetServer();
    }

    protected Server server;

    @Override
    synchronized public void setExecutorService(ExecutorService executorService) {
        if (INSTANCE.server.getThreadPool() == null) {
            INSTANCE.server.setThreadPool(new ExecutorThreadPool(executorService) {
                @Override
                protected void doStop() throws Exception {
                    // Do nothing, don't shut down the Cling ExecutorService when Jetty stops!
                }
            });
        }
    }

    @Override
    synchronized public int addConnector(String host, int port) throws IOException {
        SocketConnector connector = new SocketConnector();
        connector.setHost(host);
        connector.setPort(port);

        // Open immediately so we can get the assigned local port
        connector.open();

        // Only add if open() succeeded
        server.addConnector(connector);

        // stats the connector if the server is started (server starts all connectors when started)
        if (server.isStarted()) {
            try {
                connector.start();
            } catch (Exception ex) {
                log.severe("Couldn't start connector: " + connector + " " + ex);
                throw new RuntimeException(ex);
            }
        }
        return connector.getLocalPort();
    }

    synchronized public void removeConnector(String host, int port) {
        Connector[] connectors = server.getConnectors();
        if (connectors == null)
            return;

        for (Connector connector : connectors) {
            //Fix getPort()
            if (connector.getHost().equals(host) && connector.getLocalPort() == port) {
                if (connector.isStarted() || connector.isStarting()) {
                    try {
                        connector.stop();
                    } catch (Exception ex) {
                        log.severe("Couldn't stop connector: " + connector + " " + ex);
                        throw new RuntimeException(ex);
                    }
                }
                server.removeConnector(connector);
                if (connectors.length == 1) {
                    log.info("No more connectors, stopping Jetty server");
                    stopIfRunning();
                }
                break;
            }
        }
    }

    @Override
    synchronized public void registerServlet(String contextPath, Servlet servlet) {
        if (server.getHandler() != null) {
            return;
        }
        log.info("Registering UPnP servlet under context path: " + contextPath);
        ServletContextHandler servletHandler =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        if (contextPath != null && contextPath.length() > 0)
            servletHandler.setContextPath(contextPath);
        ServletHolder s = new ServletHolder(servlet);
        servletHandler.addServlet(s, "/*");
        server.setHandler(servletHandler);
    }

    @Override
    synchronized public void startIfNotRunning() {
        if (!server.isStarted() && !server.isStarting()) {
            log.info("Starting Jetty server... ");
            try {
                server.start();
            } catch (Exception ex) {
                log.severe("Couldn't start Jetty server: " + ex);
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    synchronized public void stopIfRunning() {
        if (!server.isStopped() && !server.isStopping()) {
            log.info("Stopping Jetty server...");
            try {
                server.stop();
            } catch (Exception ex) {
                log.severe("Couldn't stop Jetty server: " + ex);
                throw new RuntimeException(ex);
            } finally {
                resetServer();
            }
        }
    }

    protected void resetServer() {
        server = new Server(); // Has its own QueuedThreadPool
        server.setGracefulShutdown(1000); // Let's wait a second for ongoing transfers to complete
    }

    /**
     * Casts the request to a Jetty API and tries to write a space character to the output stream of the socket.
     * <p>
     * This space character might confuse the HTTP client. The Cling transports for Jetty Client and
     * Apache HttpClient have been tested to work with space characters. Unfortunately, Sun JDK's
     * HttpURLConnection does not gracefully handle any garbage in the HTTP request!
     * </p>
     */
    public static boolean isConnectionOpen(HttpServletRequest request) {
        return isConnectionOpen(request, " ".getBytes());
    }

    public static boolean isConnectionOpen(HttpServletRequest request, byte[] heartbeat) {
        Request jettyRequest = (Request) request;
        AbstractHttpConnection connection = jettyRequest.getConnection();
        Socket socket = (Socket) connection.getEndPoint().getTransport();
        if (log.isLoggable(Level.FINE))
            log.fine("Checking if client connection is still open: " + socket.getRemoteSocketAddress());
        try {
            socket.getOutputStream().write(heartbeat);
            socket.getOutputStream().flush();
            return true;
        } catch (IOException ex) {
            if (log.isLoggable(Level.FINE))
                log.fine("Client connection has been closed: " + socket.getRemoteSocketAddress());
            return false;
        }
    }
}
