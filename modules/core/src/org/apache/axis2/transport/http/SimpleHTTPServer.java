/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.apache.axis2.transport.http;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.HttpFactory;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.server.SimpleHttpServer;
import org.apache.axis2.util.OptionsParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis2.
 * It can be used with no configuration other than the port to listen on, or it can
 * be configured in detail with an HttpFactory.
 */
public class SimpleHTTPServer implements TransportListener {

    private static final Log log = LogFactory.getLog(SimpleHTTPServer.class);

    /**
     * Embedded commons http core based server
     */
    SimpleHttpServer embedded = null;
    int port = -1;

    public static int DEFAULT_PORT = 8080;

    private String hostAddress = null;
    private String contextPath;

    protected ConfigurationContext configurationContext;
    protected HttpFactory httpFactory;

    public SimpleHTTPServer() {
    }

    /** Create a SimpleHTTPServer using default HttpFactory settings */
    public SimpleHTTPServer(ConfigurationContext configurationContext, int port) throws AxisFault {
        this(new HttpFactory(configurationContext, port));
    }
    
    /** Create a configured SimpleHTTPServer */
    public SimpleHTTPServer(HttpFactory httpFactory) throws AxisFault {
        this.httpFactory = httpFactory;
        this.configurationContext = httpFactory.getConfigurationContext();
        this.port = httpFactory.getPort();
        TransportInDescription httpDescription = new TransportInDescription(new QName(Constants.TRANSPORT_HTTP));
        httpDescription.setReceiver(this);
        httpFactory.getListenerManager().addListener(httpDescription, true);
        contextPath = configurationContext.getContextPath();
    }

    /**
     * init method in TransportListener
     *
     * @param axisConf
     * @param transprtIn
     * @throws AxisFault
     */
    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault {
        try {
            this.configurationContext = axisConf;

            Parameter param = transprtIn.getParameter(PARAM_PORT);
            if (param != null)
                this.port = Integer.parseInt((String) param.getValue());
            
            if (httpFactory==null)
                httpFactory = new HttpFactory(configurationContext, port);
            
            param = transprtIn.getParameter(HOST_ADDRESS);
            if (param != null)
                hostAddress = ((String) param.getValue()).trim();
            else
                hostAddress = httpFactory.getHostAddress();

            contextPath = configurationContext.getContextPath();
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        OptionsParser optionsParser = new OptionsParser(args);

        args = optionsParser.getRemainingArgs();
        // first check if we should print usage
        if ((optionsParser.isFlagSet('?') > 0) || (optionsParser.isFlagSet('h') > 0) ||
                args == null || args.length == 0 || args.length > 2) {
            printUsage();
        }
        String paramPort = optionsParser.isValueSet('p');
        if (paramPort != null) {
            port = Integer.parseInt(paramPort);
        }
        args = optionsParser.getRemainingArgs();

        System.out.println("[SimpleHTTPServer] Starting");
        System.out.println("[SimpleHTTPServer] Using the Axis2 Repository "
                + new File(args[0]).getAbsolutePath());
        System.out.println("[SimpleHTTPServer] Listening on port " + port);
        try {
            ConfigurationContext configctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(args[0], null);
            SimpleHTTPServer receiver = new SimpleHTTPServer(configctx, port);
            Runtime.getRuntime().addShutdownHook(new ShutdownThread(receiver));
            receiver.start();
            ListenerManager listenerManager =configctx .getListenerManager();
            TransportInDescription trsIn = new TransportInDescription(
                    new QName(Constants.TRANSPORT_HTTP));
            trsIn.setReceiver(receiver);
            if (listenerManager == null) {
                listenerManager = new ListenerManager();
                listenerManager.init(configctx);
            }
            listenerManager.addListener(trsIn, true);
            System.out.println("[SimpleHTTPServer] Started");
        } catch (Throwable t) {
            log.fatal("Error starting SimpleHTTPServer", t);
            System.out.println("[SimpleHTTPServer] Shutting down");
        }
    }

    public static void printUsage() {
        System.out.println("Usage: SimpleHTTPServer [options] <repository>");
        System.out.println(" Opts: -? this message");
        System.out.println();
        System.out.println("       -p port to listen on (default is 8080)");
        System.exit(1);
    }


    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws AxisFault {
        try {
            embedded = new SimpleHttpServer(httpFactory, port);
            embedded.init();
            embedded.start();
        } catch (IOException e) {
            log.error(e);
            throw new AxisFault(e);
        }
    }

    /**
     * Stop this server. Can be called safely if the system is already stopped,
     * or if it was never started.
     * This will interrupt any pending accept().
     */
    public void stop() {
        System.out.println("[SimpleHTTPServer] Stop called");
        if (embedded != null) {
            try {
                embedded.destroy();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
    
    /** Getter for httpFactory */
    public HttpFactory getHttpFactory() {
        return httpFactory;
    }

    /**
     * Method getConfigurationContext
     *
     * @return the system context
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * replyToEPR
     * If the user has given host address paramter then it gets the high priority and
     * ERP will be creatd using that
     * N:B - hostAddress should be a complte url (http://www.myApp.com/ws)
     *
     * @param serviceName
     * @param ip
     * @return an EndpointReference
     * @see org.apache.axis2.transport.TransportListener#getEPRForService(String,String)
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        //if host address is present
        if (hostAddress != null) {
            if (embedded != null) {
                return new EndpointReference(hostAddress + contextPath + "/" + serviceName);
            } else {
                throw new AxisFault("Unable to generate EPR for the transport : http");
            }
        }
        //if the host address is not present
        String localAddress;
        if (ip != null) {
            localAddress = ip;
        } else {
            try {
                localAddress = HttpUtils.getIpAddress();
            } catch (SocketException e) {
                throw AxisFault.makeFault(e);
            }
        }
        if (embedded != null) {
            return new EndpointReference("http://" + localAddress + ":" +
                    (embedded.getPort())
                    + contextPath + "/" + serviceName);
        } else {
            throw new AxisFault("Unable to generate EPR for the transport : http");
        }
    }

    /**
     * Checks if this HTTP server instance is running.
     *
     * @return true/false
     */
    public boolean isRunning() {
        if (embedded == null) {
            return false;
        }

        return embedded.isRunning();
    }

    static class ShutdownThread extends Thread {
        private SimpleHTTPServer server = null;

        public ShutdownThread(SimpleHTTPServer server) {
            super();
            this.server = server;
        }

        public void run() {
            System.out.println("[SimpleHTTPServer] Shutting down");
            server.stop();
            System.out.println("[SimpleHTTPServer] Shutdown complete");
        }
    }
}
