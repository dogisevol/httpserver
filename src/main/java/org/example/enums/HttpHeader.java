package org.example.enums;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public enum HttpHeader {
    CONTENT_TYPE("Content-Type");

    private final String header;

    private HttpHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}