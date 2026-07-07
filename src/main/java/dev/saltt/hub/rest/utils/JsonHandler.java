package dev.saltt.hub.rest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.saltt.hub.rest.ValidationException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JsonHandler<REQ> implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(JsonHandler.class.getName());
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long MAX_BODY_BYTES = 1_000_000; // 1 MB guard

    private final Class<REQ> type;
    private final byte[] tokenBytes;

    protected JsonHandler(Class<REQ> type, String token) {
        this.type = type;
        this.tokenBytes = token.getBytes(StandardCharsets.UTF_8);
    }

    /** Validate + map + persist. Return a body object to serialize as the 200 response. */
    protected abstract Object process(REQ request) throws ValidationException;

    @Override
    public final void handle(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                respond(ex, 405, new ErrorResponse("method not allowed"));
                return;
            }
            if (!authorized(ex)) {
                respond(ex, 401, new ErrorResponse("unauthorized"));
                return;
            }

            REQ request;
            try (var body = ex.getRequestBody()) {
                byte[] raw = body.readNBytes((int) MAX_BODY_BYTES);
                request = MAPPER.readValue(raw, type);
            } catch (Exception parse) {
                respond(ex, 400, new ErrorResponse("malformed request: " + parse.getMessage()));
                return;
            }

            try {
                respond(ex, 200, process(request));
            } catch (ValidationException v) {
                respond(ex, 400, new ErrorResponse(v.getMessage()));
            }

        } catch (Exception unexpected) {
            LOG.log(Level.SEVERE, "Unhandled error in " + getClass().getSimpleName(), unexpected);
            respond(ex, 500, new ErrorResponse("internal error"));
        } finally {
            ex.close();
        }
    }

    private boolean authorized(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return false;
        byte[] provided = header.substring(7).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(provided, tokenBytes); // constant-time
    }

    private void respond(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public record ErrorResponse(String error) {}
}