package ru.liner.vr360server.activity;

import androidx.annotation.NonNull;

import java.net.Socket;

public interface IDataReceiver {
    void onClientConnected(Socket socket);
    void onClientDisconnected(Socket socket);
    void onClientDataReceived(Socket socket, @NonNull String data);
}
