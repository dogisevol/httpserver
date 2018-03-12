package org.example.server.impl;

import org.example.enums.HttpHeader;
import org.example.enums.HttpStatus;
import org.example.server.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse implements Response {

    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();

    private final SocketChannel channel;
    private String version = "HTTP/1.1";
    private int responseCode = HttpStatus.OK.getCode();
    private String responseReason = HttpStatus.OK.name();
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private WritableByteChannel content;
    private int contentLength = 0;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public HttpResponse(SocketChannel channel) throws IOException {
        this.channel = channel;
        content = Channels.newChannel(baos);
    }

    private void writeLine(String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
    }

    @Override
    public void print(String string) throws IOException {
        contentLength += content.write(encoder.encode(CharBuffer.wrap(string)));
    }

    @Override
    public void println(String string) throws IOException {
        contentLength += content.write(encoder.encode(CharBuffer.wrap(string + "\r\n")));
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    @Override
    public void writeContent(byte[] bytes) throws IOException {
        contentLength += content.write(ByteBuffer.wrap(bytes));
    }

    @Override
    public void send() {
        try {
            writeLine(this.getVersion() + " " + this.getResponseCode() + " " + this.getResponseReason());
            headers.put("Date", new Date().toString());
            headers.put("Connection", "close");
            headers.put("Content-Length", Integer.toString(contentLength));
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                writeLine(header.getKey() + ": " + header.getValue());
            }
            writeLine("");
            ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
            while (wrap.hasRemaining())
                channel.write(wrap);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
        } finally {
            close();
        }
    }

    public void close() {
        try {
            channel.close();
            content.close();
        } catch (IOException ex) {
        }
    }


    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public void setContentType(String contentType) {
        addHeader(HttpHeader.CONTENT_TYPE.getHeader(), contentType);
    }

    @Override
    public String getContentType() {
        return getHeader(HttpHeader.CONTENT_TYPE.getHeader());
    }

    @Override
    public void setResponseStatus(HttpStatus status) {
        setResponseCode(status.getCode());
        setResponseReason(status.name());
    }

    public String getResponseReason() {
        return responseReason;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}