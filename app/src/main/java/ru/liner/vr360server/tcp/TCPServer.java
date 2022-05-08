package ru.liner.vr360server.tcp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.CallSuper;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class TCPServer {
    private static final String TAG = TCPServer.class.getSimpleName();
    private ServerSocket serverSocket;
    private Callback callback;
    private final Handler handler;
    private List<Socket> clientSocketList;
    private final int port;

    public TCPServer(int port) {
        this.port = port;
        this.clientSocketList = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public List<Socket> getClientSocketList() {
        return clientSocketList;
    }

    public void sendToAll(byte[] bytes){
        if (!(bytes == null || clientSocketList == null)) {
            for (Socket socket : clientSocketList) {
                try {
                    socket.getOutputStream().write(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendToAll(String string){
        sendToAll(string.getBytes(StandardCharsets.UTF_8));
    }

    public void sendTo(Socket socket, byte[] bytes){
        if (bytes != null && socket != null && socket.isConnected()) {
            try {
                socket.getOutputStream().write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendTo(Socket socket, String string){
        sendTo(socket, string.getBytes(StandardCharsets.UTF_8));
    }

    public void start(Callback callback) {
        this.callback = callback;
        try {
            this.serverSocket = new ServerSocket(port);
            this.clientSocketList = new ArrayList<>();
            new ServerThread().execute();
            callback.onStarted(this);
        } catch (Exception e) {
            Log.e(TAG, "Unable to start server", e);
        }
    }

    public void stop(){
        if (clientSocketList != null) {
            for (Socket socket : clientSocketList) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                callback.onStopped(this);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverSocket = null;
            }
        }
    }

    public boolean isRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }


    public class SocketThread implements Runnable {
        private final Socket socket;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                if (isRunning()) {
                    while (true) {
                        byte[] byteBuffer = new byte[2048];
                        int size = inputStream.read(byteBuffer);
                        if (size <= 0)
                            break;
                        handler.post(() -> {
                            callback.onReceived(socket, Arrays.copyOfRange(byteBuffer, 0, size));
                            callback.onReceived(socket, new String(Arrays.copyOfRange(byteBuffer, 0, size)));
                        });
                    }
                }
                if (clientSocketList != null) {
                    clientSocketList.remove(socket);
                    handler.post(() -> callback.onDisconnected(socket));
                }
            } catch (Exception e) {
                if (clientSocketList != null) {
                    clientSocketList.remove(socket);
                    handler.post(() -> callback.onDisconnected(socket));
                }
                e.printStackTrace();
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class ServerThread extends AsyncTask<Void, byte[], Void> {
        @Override
        protected void onPreExecute() {
            clientSocketList = new ArrayList<>();
        }

        @Override
        public Void doInBackground(Void... params) {
            while (isRunning()) {
                try {
                    Socket socket = serverSocket.accept();
                    clientSocketList.add(socket);
                    handler.post(() -> callback.onConnected(socket));
                    new Thread(new SocketThread(socket)).start();
                } catch (IOException ignored) {
                }
            }
            return null;
        }
    }


    public interface Callback {
        @CallSuper
        default void onStarted(TCPServer server) {
            Log.d(TAG, "onStarted: "+server.toString());

        }

        @CallSuper
        default void onConnected(Socket socket) {
            Log.d(TAG, "onConnected: "+socket.toString());

        }

        @CallSuper
        default void onReceived(Socket socket, byte[] bytes) {
        }

        @CallSuper
        default void onReceived(Socket socket, String string) {
            if(!string.equals("check_ping"))
                Log.d(TAG, "onReceived: "+socket.getInetAddress().toString()+" | "+string);

        }

        @CallSuper
        default void onDisconnected(Socket socket) {
            Log.d(TAG, "onDisconnected: "+socket.toString());

        }

        @CallSuper
        default void onStopped(TCPServer server) {
            Log.d(TAG, "onStopped: "+server.toString());
        }
    }
}
