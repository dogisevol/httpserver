package org.example.server;

import org.example.enums.HttpStatus;

import java.io.IOException;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public interface Response {
    public void send();

    public void writeContent(byte[] bytes) throws IOException;

    void print(String string) throws IOException;

    void println(String string) throws IOException;

    void addHeader(String name, String value);

    //void setResponseCode(int number);

    //void setResponseReason(String s);

    int getResponseCode();

    void setContentType(String contentType);

    String getContentType();

    void setResponseStatus(HttpStatus unsupportedMediaType);
}
