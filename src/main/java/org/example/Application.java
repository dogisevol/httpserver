package org.example;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public class Application {
    public static final String DEFAULT_HOST = "localhost";
    private static int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        List<String> options = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.charAt(0) == '-') {
                if (args.length <= i + 1 || "-help".equals(arg) || args[i + 1].charAt(0) == '-') {
                    printUsage();
                    return;
                } else {
                    params.put(arg.substring(1), args[++i]);
                }
            } else {
                options.add(arg);
            }
        }

        Path documentBase;
        String docBaseDir = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File root = new File(URLDecoder.decode(docBaseDir, "UTF-8"));
        documentBase = root.isDirectory() ? root.toPath() : root.getParentFile().toPath();
        String hostName = DEFAULT_HOST;
        String h = params.get("h");
        if (h != null) {
            hostName = h;
        }

        String p = params.get("p");
        int prt = DEFAULT_PORT;
        if (p != null) {
            try {
                prt = Integer.valueOf(p);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid port format");
            }
        }
        String d = params.get("d");
        if (d != null) {
            File file = new File(d);
            if (file.exists()) {
                documentBase = file.toPath();
            } else {
                Path path = documentBase.resolve(file.toPath());
                if (path.toFile().exists()) {
                    documentBase = path;
                } else {
                    throw new RuntimeException("Document base does not exist");
                }
            }
        }

        final Path db = documentBase;
        final String host = hostName;
        int port = prt;
        new Thread(() -> {
            try {
                HttpServer server = new HttpServer(host, port, db);
                while (true) {
                    server.run();
                    Thread.currentThread().sleep(100);

                }
            } catch (Exception e) {
                System.err.println("Cannot run server");
                throw new RuntimeException(e);
            }
        }).start();
        System.out.printf("server started on port %s\n", port);
    }

    private static void printUsage() {
        System.out.println("Usage: [Options]");
        System.out.println("Options:");
        System.out.println("-help <arg>\tDisplay help information.");
        System.out.printf("-p <arg>     \tPort. %s will be used if not provided.\n", DEFAULT_PORT);
        System.out.printf("-h <arg>     \tHost name. %s will be used if not provided\n", DEFAULT_HOST);
        System.out.println("-d <arg>    \tDocument Base(absolute or relative)");
        System.out.println("            \tCurrent folder will be used if not provided");
    }
}
