package org.example.server;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public interface Request {
    int REQUEST_SIZE_ALLOWED = 2048;
    String getHttpMethod();

    int getRequestSize();

    String getVersion();

    String getLocation();

    void setLocation(String location);

    String getHost();

    int getPort();


}
