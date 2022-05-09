package ru.liner.vr360server.server;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 09.05.2022, понедельник
 **/
public class FileServer extends NanoHTTPD {
    private final File file;

    public FileServer(int port, File file) {
        super(port);
        this.file = file;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response response = newFixedLengthResponse(Status.NOT_FOUND, "text/html", "File not found: " + file.getAbsolutePath());
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                response = newFixedLengthResponse(Status.OK, "video/mp4", fileInputStream, fileInputStream.available());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }
}