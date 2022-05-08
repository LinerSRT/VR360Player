package ru.liner.vr360server.tcp;


import android.system.ErrnoException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class UDPMulticast implements Runnable {
    private final InetAddress inetAddress;
    private final MulticastSocket multicastSocket;
    private IMulticastCallback multicastCallback;
    private boolean isRunning;
    private final Thread multicastThread;
    private final String host;
    private int port;
    private final int bufferSize;

    public UDPMulticast(String host, int port) throws IOException {
        this(host, port, 1024 * 8);
    }

    public UDPMulticast(String host, int port, int bufferSize) throws IOException {
        this.host = host;
        this.port = port;
        this.bufferSize = bufferSize;
        this.multicastThread = new Thread(this);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.setReuseAddress(true);
        this.inetAddress = InetAddress.getByName(host);
    }

    public void start() {
        if (isRunning)
            return;
        isRunning = true;
        multicastThread.start();
    }

    public void stop() {
        if (!isRunning)
            return;
        isRunning = false;
        if (!multicastSocket.isClosed())
            multicastSocket.close();
    }

    public boolean isConnected() {
        return multicastSocket != null && multicastSocket.isConnected();
    }

    public boolean isRunning() {
        return multicastThread.isAlive() && isRunning;
    }

    public boolean isClosed() {
        return multicastSocket == null || multicastSocket.isClosed();
    }

    public boolean writeBytes(byte[] bytes) {
        if (!isRunning())
            return false;
        try {
            multicastSocket.send(new DatagramPacket(bytes, 0, bytes.length, inetAddress, port));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean writeString(String string) throws ErrnoException {
        return writeBytes(string.getBytes());
    }

    public void setMulticastCallback(IMulticastCallback multicastCallback) {
        this.multicastCallback = multicastCallback;
    }

    @Override
    public void run() {
        try {
            port = multicastSocket.getLocalPort();
            multicastSocket.joinGroup(inetAddress);
            if (multicastCallback != null)
                multicastCallback.onStarted();
            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
                try {
                    multicastSocket.receive(packet);
                } catch (SocketException ignored) {
                    if (isClosed()) {
                        break;
                    }
                }
                byte[] data = new byte[packet.getLength() - packet.getOffset()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
                if (multicastCallback != null) {
                    multicastCallback.onReceived(data);
                    multicastCallback.onReceived(new String(data));
                }
            }
            if (!isClosed()) {
                multicastSocket.leaveGroup(InetAddress.getByName(host));
            }
            stop();
            if (multicastCallback != null)
                multicastCallback.onStopped();
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }
}