package org.example.server.impl;

import org.example.enums.ContentType;
import org.example.enums.HttpMethod;
import org.example.enums.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.Servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
            if (request.getLocation() != null && request.getLocation().startsWith("/images/")) {
                readResource(request, response);
            } else {
                getContent(request, response);
            }
        }
        response.send();
    }

    private void validateRequest(Request request, Response response) {
        String location = request.getLocation();
        if (!HttpMethod.GET.toString().equals(request.getHttpMethod())) {
            //response.setResponseStatus(HttpStatus.METHOD_NOT_ALLOWED);
        } else if (request.getRequestSize() >= Request.REQUEST_SIZE_ALLOWED) {
            response.setResponseStatus(HttpStatus.REQUEST_URI_TOO_LARGE);
        } else if (!("HTTP/1.0".equals(request.getVersion()) || "HTTP/1.1".equals(request.getVersion()))) {
            response.setResponseStatus(HttpStatus.BAD_REQUEST);
        } else if (location.contains("/..") ||
                location.contains("../") ||
                location.contains("/.ht") || location.endsWith("~")) {
            response.setResponseStatus(HttpStatus.FORBIDDEN);
        }
    }


    private void getContent(Request request, Response response) {
        try {
            String location = request.getLocation();
            File file = new File(docBase.toFile(), location);
            if (file.exists()) {
                if (file.isDirectory()) {
                    readDir(request, response);
                } else {
                    readFile(file, response);
                }
            } else {
                response.setResponseStatus(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.setResponseStatus(HttpStatus.NOT_FOUND);
        }

    }

    private void readResource(Request request, Response response) throws IOException {
        InputStream in = getClass().getResourceAsStream(request.getLocation());
        if (in != null) {
            ReadableByteChannel channel = Channels.newChannel(in);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                if (channel.read(buffer) == -1) {
                    break;
                }
                buffer.flip();
                response.writeContent(buffer.array());
                buffer.clear();
            }
        }

        setContentType(request.getLocation(), response);

    }

    private void readFile(File file, Response response) throws IOException {
        setContentType(file.getName(), response);
        if (response.getContentType() != null)
            response.writeContent(Files.readAllBytes(file.toPath()));
        else {
            response.setResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

    }

    private void setContentType(String name, Response response) {
        String extension = getFileExtension(name);
        String ext = extension;
        Arrays.stream(ContentType.values()).forEach(type ->
                {
                    Optional<String> any = Arrays.stream(type.getFileExtension())
                            .filter(e -> e.equals(ext)).findFirst();
                    any.ifPresent(s -> response.setContentType(type.getContentType()));
                }
        );
    }

    private String getFileExtension(String name) {
        name = name.toLowerCase();
        String extension = "";

        int i = name.lastIndexOf('.');
        if (i > 0) {
            extension = name.substring(i + 1);
        }
        return extension;
    }

    private void readDir(Request request, Response response) throws IOException {
        if (!request.getLocation().endsWith("/")) {
            response.setResponseStatus(HttpStatus.MOVED_PERMANENTLY);
            response.addHeader("Location",
                    request.getLocation() + "/");
        } else {
            response.setContentType(ContentType.HTML.getContentType());
            response.println("<html><body><table><tr><td colspan=2><center><h2>File Explorer</h2></center></td><tr>");
            Files.list(Paths.get(docBase.toAbsolutePath() + request.getLocation()))
                    .forEach(path -> {
                        try {
                            printFileLink(path, request, response);
                        } catch (IOException e) {
                            response.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    });
            response.println("</table></body></html>");
        }
    }

    private void printFileLink(Path path, Request request, Response response) throws IOException {
        String name = path.toFile().getName();
        response.print("<tr><td><img height=24 weight=24 src='/images/");
        if (path.toFile().isFile()) {
            response.print(getIcon(getFileExtension(path.toString())));
        } else {
            response.print("folder.png");
        }
        response.print("'></td><td>");
        response.print("<a");
        if (path.toFile().isFile()) {
            response.print(" target=_blank ");
        }
        response.print(" href='");
        response.print(request.getLocation());
        response.print(name);
        response.print("'>");
        response.print(name);
        response.print("</a></td></tr>");
    }

    private String getIcon(final String ext) {
        ContentType[] ct = new ContentType[1];
        Arrays.stream(ContentType.values()).forEach(type ->
                {
                    Optional<String> any = Arrays.stream(type.getFileExtension())
                            .filter(e -> e.equals(ext)).findFirst();
                    any.ifPresent(s -> ct[0] = type);
                }
        );
        if (ct[0] != null) {
            for (String extension : ct[0].getFileExtension()) {
                if (extension.equals(ext)) {
                    String fileName = extension + ".png";
                    InputStream st = this.getClass().getClassLoader().getResourceAsStream("images/" + fileName);
                    if (st != null)
                        return extension + ".png";
                }
            }
        }
        return "file.png";
    }
}
