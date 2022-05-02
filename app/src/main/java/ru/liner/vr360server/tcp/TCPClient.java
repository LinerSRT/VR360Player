package ru.liner.vr360server.tcp;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class TCPClient {
    private Callback callback;
    private String host;
    private int port;
    private Socket socket;
    private boolean connected;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start(Callback callback) {
        this.callback = callback;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000);
            callback.onStarted(socket);
            new ClientHandlerThread().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                callback.onStopped(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }
        }
    }

    public void send(byte[] bytes){
        if (bytes != null && socket != null && socket.isConnected()) {
            try {
                socket.getOutputStream().write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void send(String string){
        send(string.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isConnected() {
        return connected;
    }

    private class ClientHandlerThread extends AsyncTask<Void, byte[], Void> {
        private InputStream inputStream;

        private ClientHandlerThread() {
            this.inputStream = null;
        }

        @Override
        protected void onPreExecute() {
            callback.onConnected(socket);
            connected = true;
            try {
                this.inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Void doInBackground(Void... params) {
            byte[] content = new byte[2048];
            if (inputStream == null)
                return null;
            while (true) {
                try {
                    int bytesRead = inputStream.read(content);
                    if (bytesRead == -1)
                        return null;
                    callback.onReceived(socket, Arrays.copyOfRange(content, 0, bytesRead));
                    callback.onReceived(socket, new String(Arrays.copyOfRange(content, 0, bytesRead)));
                } catch (Exception e) {
                    return null;
                }
            }
        }

        @Override
        public void onPostExecute(Void v) {
            try {
                socket.close();
                callback.onDisconnected(socket);
                connected = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface Callback {
        void onStarted(Socket socket);

        void onConnected(Socket socket);

        default void onReceived(Socket socket, byte[] bytes) {
        }

        default void onReceived(Socket socket, String string) {
        }

        void onDisconnected(Socket socket);

        void onStopped(Socket socket);
    }
}
