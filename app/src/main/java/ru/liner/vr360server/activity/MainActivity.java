package ru.liner.vr360server.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.skydoves.androidbottombar.AndroidBottomBarView;
import com.skydoves.androidbottombar.BottomMenuItem;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.fragments.DevicesFragment;
import ru.liner.vr360server.fragments.SettingsFragment;
import ru.liner.vr360server.fragments.VideosFragment;
import ru.liner.vr360server.server.ConnectedClient;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.FragmentAdapter;
import ru.liner.vr360server.utils.ViewUtils;
import ru.liner.vr360server.utils.pagetransformer.ParallaxTransformer;
import ru.liner.vr360server.views.ExpandLayout;
import ru.liner.vr360server.views.ExtendedViewPager;


public class MainActivity extends CoreActivity implements IServer {
    private AndroidBottomBarView bottomNavigation;
    private ExtendedViewPager viewPager;
    private ExpandLayout notificationLayout;
    private TextView notificationTitle;
    private TextView notificationText;
    private List<Socket> socketList;
    public static TCPServer tcpServer;
    private IPPublisher ipPublisher;
    private List<IDataReceiver> dataReceiverList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        notificationLayout = findViewById(R.id.notificationLayout);
        notificationTitle = findViewById(R.id.notificationTitle);
        notificationText = findViewById(R.id.notificationText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        viewPager = findViewById(R.id.viewPager);
        socketList = new ArrayList<>();
        dataReceiverList = new ArrayList<>();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.add(new DevicesFragment(this));
        fragmentAdapter.add(new VideosFragment(this));
        fragmentAdapter.add(new SettingsFragment(this));
        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(false, new ParallaxTransformer());
        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();
        bottomMenuItems.add(new BottomMenuItem(this)
                .setTitle("Devices")
                .setIcon(R.drawable.ic_baseline_devices_other_24)
                .setIconSize(24)
                .build());
        bottomMenuItems.add(new BottomMenuItem(this)
                .setTitle("Videos")
                .setIcon(R.drawable.ic_baseline_folder_24)
                .setIconSize(24)
                .build());
        bottomMenuItems.add(new BottomMenuItem(this)
                .setTitle("Settings")
                .setIcon(R.drawable.ic_baseline_settings_24)
                .setIconSize(24)
                .build());
        bottomNavigation.addBottomMenuItems(bottomMenuItems);
        bottomNavigation.setOnBottomMenuInitializedListener(() -> {
            bottomNavigation.bindViewPager(viewPager);
            viewPager.setCurrentItem(0);
        });
        bottomNavigation.setOnMenuItemSelectedListener((i, bottomMenuItem, b) -> viewPager.setCurrentItem(i));
    }

    @Override
    public void register(IDataReceiver dataReceiver) {
        if(!dataReceiverList.contains(dataReceiver))
        dataReceiverList.add(dataReceiver);
    }

    @Override
    public void unregister(IDataReceiver dataReceiver) {
        dataReceiverList.remove(dataReceiver);
    }

    @Override
    public void startServer() {
        if (tcpServer == null)
            tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.start(new TCPServer.Callback() {
            @Override
            public void onStarted(TCPServer tcpServer) {
                TCPServer.Callback.super.onStarted(tcpServer);
                if (ipPublisher == null)
                    ipPublisher = new IPPublisher();
                ipPublisher.start();
            }

            @Override
            public void onConnected(Socket socket) {
                TCPServer.Callback.super.onConnected(socket);
                if (!isConnected(socket)) {
                    socketList.add(socket);
                    runOnUiThread(() -> onSocketConnected(socket, socketList.size() - 1));
                }
            }

            @Override
            public void onDisconnected(Socket socket) {
                TCPServer.Callback.super.onDisconnected(socket);
                int index = getSocketIndex(socket);
                if (index != -1) {
                    socketList.remove(index);
                    runOnUiThread(() -> onSocketDisconnected(socket, index));
                }
            }

            @Override
            public void onStopped(TCPServer tcpServer) {
                TCPServer.Callback.super.onStopped(tcpServer);
                if (ipPublisher != null) {
                    ipPublisher.interrupt();
                    ipPublisher = null;
                }
            }


            @Override
            public void onReceived(Socket socket, String data) {
                TCPServer.Callback.super.onReceived(socket, data);
                int index = getSocketIndex(socket);
                if (index != -1) {
                    runOnUiThread(() -> onReceived(socket, data));
                }
            }
        });
    }

    @Override
    public void stopServer() {
        if (tcpServer != null)
            tcpServer.stop();
    }

    @Override
    public boolean isServerRunning() {
        return tcpServer != null && tcpServer.isRunning();
    }

    @CallSuper
    @Override
    public void onSocketConnected(Socket socket, int position) {
        for(IDataReceiver dataReceiver:dataReceiverList)
            dataReceiver.onSocketConnected(socket, position);
    }

    @CallSuper
    @Override
    public void onSocketDisconnected(Socket socket, int position) {
        for(IDataReceiver dataReceiver:dataReceiverList)
            dataReceiver.onSocketDisconnected(socket, position);
    }

    @CallSuper
    @Override
    public void onReceived(Socket socket, String command) {
        for(IDataReceiver dataReceiver:dataReceiverList)
            dataReceiver.onReceived(socket, command);
    }


    @Override
    public void send(String command) {
        if (isServerRunning())
            tcpServer.sendToAll(command);
    }

    @Override
    public void sendToSocket(Socket socket, String command) {
        if (isServerRunning())
            tcpServer.sendTo(socket, command);
    }

    @Override
    public List<Socket> getSocketList() {
        return socketList;
    }

    @Override
    public boolean hasConnectedSockets() {
        return !socketList.isEmpty();
    }

    @Override
    public void showNotification(String title, String message, @ColorRes int backgroundColor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(notificationLayout.isExpanded()) {
                    notificationLayout.setOnExpandCallback(new ExpandLayout.OnExpandCallback() {
                        @Override
                        public void onExpanded(ExpandLayout expandLayout) {

                        }

                        @Override
                        public void onCollapsed(ExpandLayout expandLayout) {
                            ViewUtils.setStatusBarColor(MainActivity.this, ContextCompat.getColor(MainActivity.this, R.color.backgroundColor));
                            notificationTitle.setText(title);
                            notificationText.setText(message);
                            notificationLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, backgroundColor));

                            ViewUtils.setStatusBarColor(MainActivity.this, ContextCompat.getColor(MainActivity.this, backgroundColor));
                            notificationLayout.expand();
                        }
                    });
                    notificationLayout.collapse();
                } else {
                    notificationTitle.setText(title);
                    notificationText.setText(message);
                    notificationLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, backgroundColor));
                    ViewUtils.setStatusBarColor(MainActivity.this, ContextCompat.getColor(MainActivity.this, backgroundColor));
                    notificationLayout.expand();
                }
            }
        });
    }

    @Override
    public void dismissNotification() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notificationLayout.setOnExpandCallback(new ExpandLayout.OnExpandCallback() {
                    @Override
                    public void onExpanded(ExpandLayout expandLayout) {

                    }

                    @Override
                    public void onCollapsed(ExpandLayout expandLayout) {
                        ViewUtils.setStatusBarColor(MainActivity.this, ContextCompat.getColor(MainActivity.this, R.color.backgroundColor));
                    }
                });
                notificationLayout.collapse();

            }
        });
    }

    @Override
    public String serialize(Object object) {
        if(object == null)
            return "";
        return object.getClass().getSimpleName()+"@"+new Gson().toJson(object);
    }

    @Override
    public void send(ConnectedClient client, Object object) {
        sendToSocket(client.getSocket(), serialize(object));
    }

    @Override
    public void send(ConnectedClient client, String command) {
        sendToSocket(client.getSocket(), command);
    }

    @Override
    public boolean isConnected(Socket socket) {
        if (socketList.isEmpty())
            return false;
        for (Socket s : socketList)
            if(socket.getInetAddress().getHostAddress().equals(s.getInetAddress().getHostAddress()) && s.getLocalPort() == socket.getLocalPort())
                return true;
        return false;
    }

    @Override
    public void disconnect(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getSocketIndex(Socket socket) {
        for (int i = 0; i < socketList.size(); i++) {
            Socket s = socketList.get(i);
            if(socket.getInetAddress().getHostAddress().equals(s.getInetAddress().getHostAddress()) && s.getLocalPort() == socket.getLocalPort())
                return i;
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
    }
}
