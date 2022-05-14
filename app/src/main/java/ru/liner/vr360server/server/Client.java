package ru.liner.vr360server.server;

import androidx.annotation.Nullable;

import java.net.Socket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class Client {
    public transient Socket socket;
    public String hostname;
    public boolean waitingAction = true;
    public boolean readyAction = true;
    public boolean playingVideo = false;

    public Client(Socket socket, String hostname) {
        this.socket = socket;
        this.hostname = hostname;
    }
}
