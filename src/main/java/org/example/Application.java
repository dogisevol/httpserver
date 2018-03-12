package org.example;

import java.net.InetSocketAddress;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public class Application {
    private static int DEFAULT_PORT = 8081;
    private static HttpServer server;


    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port is not specified. Running server on default port: " + DEFAULT_PORT);
            }
        }

        server = new HttpServer("localhost", port, null);
        while (true) {
            server.run();
            Thread.currentThread().sleep(100);
        }
    }

    public static void shutdown(){
        server.shutdown();
    }
}
