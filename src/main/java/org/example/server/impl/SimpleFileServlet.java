package org.example.server.impl;

import org.example.enums.ContentType;
import org.example.enums.HttpMethod;
import org.example.enums.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.Servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class SimpleFileServlet implements Servlet {

    private final Path docBase;

    public SimpleFileServlet(Path docBase) {
        this.docBase = docBase;
    }

    @Override
    public void process(Request request, Response response) throws IOException {
        validateRequest(request, response);
        if (HttpStatus.OK.getCode() == response.getResponseCode()) {
            getContent(request, response);
        }
        response.send();
    }

    private void validateRequest(Request request, Response response) {
        String location = request.getLocation();
        if (!HttpMethod.GET.toString().equals(request.getHttpMethod())) {
            response.setResponseStatus(HttpStatus.METHOD_NOT_ALLOWED);
        } else if (request.getRequestSize() >= request.REQUEST_SIZE_ALLOWED) {
            response.setResponseStatus(HttpStatus.REQUEST_URI_TOO_LARGE);
        } else if (!("HTTP/1.0".equals(request.getVersion()) || "HTTP/1.1".equals(request.getVersion()))) {
            response.setResponseStatus(HttpStatus.BAD_REQUEST);
        } else if (location.indexOf("/..") != -1 ||
                location.indexOf("../") != -1 ||
                location.indexOf("/.ht") != -1 || location.endsWith("~")) {
            response.setResponseStatus(HttpStatus.FORBIDDEN);
        }
    }


    private void getContent(Request request, Response response) throws IOException {
        String location = request.getLocation();
        File file = new File(docBase.toFile(), location);
        if (file.exists()) {
            if (file.isDirectory()) {
                readDir(request, response);
            } else {
                readFile(file, request, response);
            }
        } else {
            response.setResponseStatus(HttpStatus.NOT_FOUND);
        }

    }

    private void readFile(File file, Request request, Response response) throws IOException {
        String name = file.getName();
        String extension = "";

        int i = name.lastIndexOf('.');
        if (i > 0) {
            extension = name.substring(i + 1);
        }
        String ext = extension;
        Arrays.stream(ContentType.values()).forEach(type ->
                {
                    Optional<String> any = Arrays.stream(type.getFileExtension())
                            .filter(e -> e.equals(ext)).findFirst();
                    if (any.isPresent()) {
                        response.setContentType(type.getContentType());
                    }
                }
        );
        if (response.getContentType() != null)
            response.writeContent(Files.readAllBytes(file.toPath()));
        else {
            response.setResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

    }

    private void readDir(Request request, Response response) throws IOException {
        if (!request.getLocation().endsWith("/")) {
            response.setResponseStatus(HttpStatus.MOVED_PERMANENTLY);
            response.addHeader("Location",
                    request.getLocation() + "/");
        } else {
            response.setContentType(ContentType.HTML.getContentType());
            response.println("<html><body>");
            Files.list(Paths.get(docBase.toAbsolutePath() + request.getLocation()))
                    .forEach(path -> {
                        try {
                            printFileLink(path, request, response);
                        } catch (IOException e) {
                            response.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    });
            response.println("</body></html>");
        }
    }

    private void printFileLink(Path path, Request request, Response response) throws IOException {
        String name = path.toFile().getName();
        response.print("<br>");
        response.print("<a href='");
        response.print(request.getLocation());
        response.print(name);
        response.print("'>");
        response.print(name);
        response.print("</a>");
    }
}
