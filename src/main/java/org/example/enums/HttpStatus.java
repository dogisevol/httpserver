package org.example.enums;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public enum HttpStatus {
    CONTINUE(100),
    OK(200),
    NO_CONTENT(204),
    PARTIAL_CONTENT(206),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    REQUEST_TIMEOUT(408),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(412),
    REQUEST_URI_TOO_LARGE(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503);

    private final int code;

    private HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}