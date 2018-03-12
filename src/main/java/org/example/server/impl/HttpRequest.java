package org.example.server.impl;

import org.example.server.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpRequest implements Request {
    private final int port;
    private String httpMethod;
    private String location;
    private Map<String, String> headers = new HashMap<String, String>();
    private final ByteBuffer buffer = ByteBuffer.allocate(REQUEST_SIZE_ALLOWED);
    private int requestSize = 0;
    private SocketChannel channel;
    private String version;
    private String host;


    public HttpRequest(SocketChannel channel, String host, int port) throws IOException {
        this.channel = channel;
        this.host = host;
        this.port = port;
        process();
    }


    public void process() throws IOException {
        buffer.limit(buffer.capacity());
        requestSize = channel.read(buffer);
        if (requestSize > -1) {
            requestSize += channel.read(buffer);
            if (requestSize < REQUEST_SIZE_ALLOWED) {
                buffer.flip();
                readHeaders();
            }
        }
    }

    private void readHeaders() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        int previous = -1;
        while (buffer.hasRemaining()) {
            char current = (char) buffer.get();
            builder.append(current);
            if (current == '\n' && previous == '\r') {
                processRequestHead(builder.toString());
                break;
                //TODO for now we only parse GET request. read request body for POST and PUT
                //builder = new StringBuilder();
            }
            previous = current;
        }
    }

    private void processRequestHead(String requestHead) throws UnsupportedEncodingException {
        StringTokenizer tokenizer = new StringTokenizer(requestHead);
        httpMethod = tokenizer.nextToken().toUpperCase();
        location = tokenizer.nextToken();
        if (location != null)
            location = URLDecoder.decode(location, "UTF-8");
        version = tokenizer.nextToken();
        String[] lines = requestHead.split("\r\n");
        for (int i = 1; i < lines.length; i++) {
            String[] keyVal = lines[i].split(":", 2);
            headers.put(keyVal[0], keyVal[1]);
        }
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public int getRequestSize() {
        return requestSize;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getVersion() {
        return version;
    }
}

