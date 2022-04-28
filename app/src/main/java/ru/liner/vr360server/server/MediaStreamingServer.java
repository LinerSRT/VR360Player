package ru.liner.vr360server.server;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class MediaStreamingServer extends NanoHTTPD {
    private String mediaFilePath;
    private final int port;

    public MediaStreamingServer(String mediaFilePath, int port) {
        super(port);
        this.mediaFilePath = mediaFilePath;
        this.port = port;
    }

    public MediaStreamingServer(int port) {
        super(port);
        this.mediaFilePath = null;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        if (mediaFilePath == null)
            return;
        super.start();
    }

    public void setFilePath(String mediaFilePath) {
        closeAllConnections();
        stop();
        this.mediaFilePath = mediaFilePath;
        if (isAlive()) {
            try {
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> sessionHeaders = session.getHeaders();
        Method sessionMethod = session.getMethod();
        Map<String, String> files = new HashMap<>();
        if (Method.POST.equals(sessionMethod) || Method.PUT.equals(sessionMethod)) {
            try {
                session.parseBody(files);
            } catch (IOException e) {
                return getResponse("Internal Error IO Exception: " + e.getMessage());
            } catch (ResponseException e) {
                return Response.newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }
        }
        return serveFile(mediaFilePath, sessionHeaders, new File(mediaFilePath));
    }

    private Response serveFile(String filePath, Map<String, String> sessionHeaders, File file) {
        Response response;
        String mimeType = getMimeTypeForFile(filePath);
        try {
            String fileETAG = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + file.length()).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = sessionHeaders.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            long fileSize = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileSize) {
                    response = createResponse(Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    response.addHeader("Content-Range", "bytes 0-0/" + fileSize);
                } else {
                    if (endAt < 0)
                        endAt = fileSize - 1;
                    final long dataLen = Math.max(endAt - startFrom + 1, 0);
                    FileInputStream fileInputStream = new FileInputStream(file) {
                        @Override
                        public int available() {
                            return (int) dataLen;
                        }
                    };
                    fileInputStream.skip(startFrom);
                    response = createResponse(Status.PARTIAL_CONTENT, mimeType, fileInputStream);
                    response.addHeader("Content-Length", "" + dataLen);
                    response.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileSize);
                }
                response.addHeader("ETag", fileETAG);
            } else {
                if (fileETAG.equals(sessionHeaders.get("if-none-match")))
                    response = createResponse(Status.NOT_MODIFIED, mimeType, "");
                else {
                    response = createResponse(Status.OK, mimeType, new FileInputStream(file));
                    response.addHeader("Content-Length", "" + fileSize);
                    response.addHeader("ETag", fileETAG);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response = getResponse("Forbidden: Reading file failed");
        }
        return response;
    }

    private Response createResponse(Status status, String mimeType, InputStream message) throws IOException {
        Response res = Response.newFixedLengthResponse(status, mimeType, message, message.available());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response createResponse(Status status, String mimeType, String message) {
        Response res = Response.newFixedLengthResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response getResponse(String message) {
        return createResponse(Status.OK, "text/plain", message);
    }

    public String getServerUrl(String ipAddress) {
        if (port == 80)
            return "http://$ipAddress/";
        if (ipAddress.contains(":")) {
            int pos = ipAddress.indexOf("%");
            if (pos > 0)
                ipAddress = ipAddress.substring(0, pos);
            return "http://" + ipAddress + ":" + port + "/";
        }
        return "http://" + ipAddress + ":" + port + "/";
    }
}