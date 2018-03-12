package org.example.server;

import java.io.IOException;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public interface Servlet {
    void process(Request request, Response response) throws IOException;
}
