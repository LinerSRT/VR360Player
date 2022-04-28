package ru.liner.vr360server.tcp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.utils.Networks;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class TCPServer implements Runnable {
    private ITCPCallback itcpCallback;
    private final int port;
    private boolean connected;
    private final List<TCPDevice> tcpDeviceList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
    }

    public void setITCPCallback(ITCPCallback itcpCallback) {
        this.itcpCallback = itcpCallback;
    }

    public void start() {
        connected = true;
        new Thread(this).start();
    }

    public void stop() {
        connected = false;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (connected) {
                try {
                    startClient(server.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (itcpCallback != null)
                        itcpCallback.onConnectionFailed(server.getInetAddress());
                }
            }
            tcpDeviceList.forEach(TCPDevice::stop);
            tcpDeviceList.clear();
            server.close();
            if (itcpCallback != null)
                itcpCallback.onDisconnected(server.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<TCPDevice> getClients() {
        return tcpDeviceList;
    }

    @Nullable
    public TCPDevice getClient(@NonNull String hostname) {
        for (TCPDevice device : tcpDeviceList)
            if (Networks.getHost(device.getInetAddress()).equals(hostname))
                return device;
        return null;
    }

    public void disconnectClient(@NonNull String hostname){
        TCPDevice device = getClient(hostname);
        if(device != null){
            device.stop();
            tcpDeviceList.remove(device);
        }
    }

    public void sendToAll(String string){
        tcpDeviceList.forEach(tcpDevice -> tcpDevice.send(string));
    }

    public void sendToAll(byte[] bytes){
        tcpDeviceList.forEach(tcpDevice -> tcpDevice.send(bytes));
    }


    private void startClient(Socket socket) {
        TCPDevice tcpDevice = new TCPDevice(socket);
        ITCPCallback callback = new ITCPCallback() {
            @Override
            public void onConnected(TCPDevice device) {
                if (itcpCallback != null)
                    itcpCallback.onConnected(device);
            }

            @Override
            public void onDisconnected(InetAddress inetAddress) {
                tcpDeviceList.remove(tcpDevice);
                if (itcpCallback != null)
                    itcpCallback.onDisconnected(inetAddress);
            }

            @Override
            public void onConnectionFailed(InetAddress inetAddress) {
                if (itcpCallback != null)
                    itcpCallback.onConnectionFailed(inetAddress);
            }

            @Override
            public void onReceived(InetAddress inetAddress, byte[] data) {
                if (itcpCallback != null)
                    itcpCallback.onReceived(inetAddress, data);
            }

            @Override
            public void onReceived(InetAddress inetAddress, String data) {
                if (itcpCallback != null)
                    itcpCallback.onReceived(inetAddress, data);
            }
        };
        tcpDevice.setITCPCallback(callback);
        tcpDevice.start();
        if(!tcpDeviceList.contains(tcpDevice)) {
            tcpDeviceList.add(tcpDevice);
            if (itcpCallback != null)
                itcpCallback.onConnected(tcpDevice);
        }
    }
}
