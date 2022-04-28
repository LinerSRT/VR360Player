package ru.liner.vr360server.tcp;


import java.net.Socket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class TCPClient implements Runnable {
    private ITCPCallback itcpCallback;
    private final int port;
    private final String host;
    private TCPDevice device;

    public TCPClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public void connect(ITCPCallback itcpCallback) {
        this.itcpCallback = itcpCallback;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            device = new TCPDevice(new Socket(host, port), itcpCallback);
            device.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (device != null) {
            device.stop();
            device = null;
        }
    }

    public boolean isConnected() {
        return device != null && device.isConnected();
    }

    public TCPDevice getDevice() {
        return device;
    }
}
