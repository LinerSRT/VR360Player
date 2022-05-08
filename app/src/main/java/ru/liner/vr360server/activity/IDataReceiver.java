package ru.liner.vr360server.activity;

import java.net.Socket;

public interface IDataReceiver {
    void onSocketConnected(Socket socket, int position);
    void onSocketDisconnected(Socket socket, int position);
    void onReceived(Socket socket, String command);
}
