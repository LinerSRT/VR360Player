package ru.liner.vr360player.asocket;


import java.net.DatagramPacket;
import java.util.concurrent.Executor;

public interface ISocket<T> {
    T createSocket() throws Exception;
    void start();
    void close();
    boolean isStart();
    boolean isConnected();
    boolean isClosed();
    void write(byte[] data);
    void write(DatagramPacket data);
    T getSocket();
    void setExecutor(Executor executor);
    void setOnSocketStateListener(OnSocketStateListener listener);
    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
    interface OnMessageReceivedListener {
        void onMessageReceived(byte[] data);
    }
    interface OnSocketStateListener {
        void onStarted();
        void onClosed();
        void onException(Exception e);
    }

}
