package org.example;

import org.example.server.impl.HttpRequest;
import org.example.server.impl.HttpResponse;
import org.example.server.impl.SimpleFileServlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public class HttpServer implements Runnable {

    private final String hostName;
    private final int port;
    private Selector selector = Selector.open();
    private ServerSocketChannel server = ServerSocketChannel.open();
    private Path documentBase;

    public HttpServer(String hostName, int port, Path documentBase) throws IOException, URISyntaxException {
        this.hostName = hostName;
        this.port = port;
        server.socket().bind(new InetSocketAddress(hostName, port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        if (documentBase != null) {
            this.documentBase = documentBase;
        } else {
            this.documentBase = Paths.get(ClassLoader.getSystemResource("docBase").toURI());
        }
    }


    @Override
    public final void run() {
        try {
            selector.selectNow();
            Iterator<SelectionKey> i = selector.selectedKeys().iterator();
            while (i.hasNext()) {
                SelectionKey selection = i.next();
                i.remove();
                if (!selection.isValid()) {
                    continue;
                }
                try {
                    if (selection.isAcceptable()) {
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (selection.isReadable()) {
                        SocketChannel channel = (SocketChannel) selection.channel();
                        new SimpleFileServlet(documentBase).process(new HttpRequest(channel, hostName, port), new HttpResponse(channel));
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                    shutdown();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
            throw new RuntimeException("Server crashed.", e);
        }
    }


    public void shutdown() {
        try {
            selector.close();
            server.close();
        } catch (IOException e1) {
            System.err.println("Couldn't properly close server");
            e1.printStackTrace();
        }
    }
}