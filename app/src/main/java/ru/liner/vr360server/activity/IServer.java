package ru.liner.vr360server.activity;

import androidx.annotation.ColorRes;

import java.net.Socket;
import java.util.List;

import ru.liner.vr360server.server.Client;

public interface IServer {
    void register(IDataReceiver dataReceiver);
    void unregister(IDataReceiver dataReceiver);
    void startServer();
    void stopServer();
    boolean isServerRunning();
    void onSocketConnected(Socket socket, int position);
    void onSocketDisconnected(Socket socket, int position);
    void onReceived(Socket socket, String command);
    void send(String command);
    void sendToSocket(Socket socket, String command);
    List<Socket> getSocketList();
    boolean isConnected(Socket socket);
    void disconnect(Socket socket);
    int getSocketIndex(Socket socket);
    boolean hasConnectedSockets();
    void showNotification(String title, String message, @ColorRes int backgroundColor);
    void showNotification(String title, String message, @ColorRes int backgroundColor, boolean indeterminate, int progress);
    void updateProgress(boolean indeterminate, int progress);
    void dismissNotification();
    String serialize(Object object);
    void send(Client client, Object object);
    void send(Client client, String command);
}
