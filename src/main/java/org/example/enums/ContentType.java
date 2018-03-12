package org.example.enums;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public enum ContentType {
    JAR("application/java-archive", "jar"),
    JS("application/javascript", "js"),
    JSON("application/json", "json"),
    EXE("application/octet-stream", "exe"),
    PDF("application/pdf", "pdf"),
    SEVEN_Z("application/x-7z-compressed", "7z"),
    TGZ("application/x-compressed", "tgz"),
    GZ("application/x-gzip", "gz"),
    TAR("application/x-tar", "tar"),
    XHTML("application/xhtml+xml", "xhtml"),
    ZIP("application/zip", "zip"),
    MP3("audio/mpeg", "mp3"),
    GIF("image/gif", "gif"),
    JPEG("image/jpeg", "jpg", "jpeg"),
    PNG("image/png", "png"),
    CSS("text/css", "css"),
    CSV("text/csv", "csv"),
    HTML("text/html; charset=utf-8", "htm", "html"),
    TEXT("text/plain", "txt", "text", "log"),
    XML("text/xml", "xml");

    private final String contentType;
    private final String[] fileExtension;

    private ContentType(String contentType, String... fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String[] getFileExtension() {
        return fileExtension;
    }
}
