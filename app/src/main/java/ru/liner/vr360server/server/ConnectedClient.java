package ru.liner.vr360server.server;

import androidx.annotation.Nullable;

import java.net.Socket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class ConnectedClient {
    private transient final Socket socket;
    public String hostname;
    @Nullable
    public ClientStatus clientStatus;
    @Nullable
    public DownloadingStatus downloadingStatus;
    @Nullable
    public PlayingStatus playingStatus;

    public ConnectedClient(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
