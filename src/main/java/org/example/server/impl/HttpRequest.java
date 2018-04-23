package org.example.server.impl;

import org.example.server.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpRequest implements Request {
    private int port;
    private String httpMethod;
    private String location;
    private Map<String, String> headers = new HashMap<String, String>();
    private int requestSize = 0;
    private SocketChannel channel;
    private String version;
    private String host;
    private ByteBuffer buffer = ByteBuffer.allocate(REQUEST_SIZE_ALLOWED);
    private byte[] body;
    ;


    public HttpRequest(SocketChannel channel, String host, int port) throws IOException {
        this.setChannel(channel);
        this.setHost(host);
        this.setPort(port);
        parse();
    }

    public HttpRequest() throws IOException {
    }

    public void parse() throws IOException {
        ByteBuffer buffer = parseRequest(getBuffer());
        if (getRequestSize() <= REQUEST_SIZE_ALLOWED && getRequestSize() > -1) {
            buffer.flip();
            readHeaders(buffer);
        }
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    private ByteBuffer parseRequest(ByteBuffer buffer) throws IOException {
        buffer.limit(buffer.capacity());
        setRequestSize(getChannel().read(buffer));
        if (getRequestSize() > -1) {
            setRequestSize(getRequestSize() + getChannel().read(buffer));
        }
        return buffer;
    }

    private void readHeaders(ByteBuffer buffer) throws UnsupportedEncodingException {
        int previous = -1;
        int position = 0;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            char current = (char) b;
            if (current == '\r' && previous == '\n') {
                break;
            }
            position++;
            previous = current;
        }
        byte[] bytes = new byte[position];
        buffer.position(0);
        buffer.get(bytes, 0, position);
        String head = new String(bytes, Charset.forName("UTF-8"));
        processRequestHead(head.trim());
        //TODO check if http method allows body
        this.body = new byte[buffer.limit() - buffer.position()];
        buffer.get(this.body, 0, this.body.length);
    }


    private void processRequestHead(String requestHead) throws UnsupportedEncodingException {
        StringTokenizer tokenizer = new StringTokenizer(requestHead);
        setHttpMethod(tokenizer.nextToken().toUpperCase());
        setLocation(tokenizer.nextToken());
        if (getLocation() != null)
            setLocation(URLDecoder.decode(getLocation(), "UTF-8"));
        if(location.contains("?")){
            //TODO parse query params
            location = location.substring(0, location.indexOf('?'));
        }
        try {
            setVersion(tokenizer.nextToken());
            String[] lines = requestHead.split("\r\n");
            for (int i = 1; i < lines.length; i++) {
                String[] keyVal = lines[i].split(":", 2);
                getHeaders().put(keyVal[0].trim().toUpperCase(), keyVal[1].trim());
            }
        }catch (Exception e){

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
        return getHeaders().get(key);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getVersion() {
        return version;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

