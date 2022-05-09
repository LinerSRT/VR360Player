package ru.liner.vr360server.activity;

import static ru.liner.vr360server.utils.Constant.PERMISSION_REQUEST_CORE;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.skydoves.androidbottombar.AndroidBottomBarView;
import com.skydoves.androidbottombar.BottomMenuItem;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.fragments.DevicesFragment;
import ru.liner.vr360server.fragments.SettingsFragment;
import ru.liner.vr360server.fragments.VideosFragment;
import ru.liner.vr360server.server.FileServer;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.FragmentAdapter;
import ru.liner.vr360server.utils.Lists;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.hashing.Hash;
import ru.liner.vr360server.utils.hashing.HashAlgorithm;
import ru.liner.vr360server.utils.pagetransformer.ParallaxTransformer;
import ru.liner.vr360server.views.ExtendedViewPager;


public class MainActivity extends CoreActivity implements IServer {
    private static IServer server;
    private AndroidBottomBarView bottomNavigation;
    private ExtendedViewPager viewPager;
    private List<Socket> socketList;
    public static TCPServer tcpServer;
    private FileServer mediaStreamingServer;
    private IPPublisher ipPublisher;
    private List<IDataReceiver> dataReceiverList;
    private List<Video> videoList;
    private boolean allVideosRetrieved;

    @Override
    protected void onStart() {
        super.onStart();
        server = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        Utils.requestPermissions(this);
        if (Utils.isPermissionGranted(this))
            runBackground(this::collectVideos);
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
                .setIcon(R.drawable.ic_baseline_video_library_24)
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

    @CallSuper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CORE && Utils.isPermissionGranted(this))
            runBackground(this::collectVideos);
    }

    @Override
    public void runBackground(Runnable runnable) {
        new Thread(runnable).start();
    }

    @Override
    public void runOnUI(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override
    public void registerDataReceiver(IDataReceiver dataReceiver) {
        if (!dataReceiverList.contains(dataReceiver))
            dataReceiverList.add(dataReceiver);
    }

    @Override
    public void unregisterDataReceiver(IDataReceiver dataReceiver) {
        dataReceiverList.remove(dataReceiver);
    }

    @Override
    public void startTCPServer() {
        if (isTCPServerRunning())
            return;
        if (tcpServer == null)
            tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.start(new TCPServer.Callback() {
            @Override
            public void onStarted(TCPServer tcpServer) {
                if (ipPublisher == null)
                    ipPublisher = new IPPublisher();
                ipPublisher.start();
            }

            @Override
            public boolean acceptConnection(Socket socket) {
                return !isClientConnected(socket);
            }

            @Override
            public void onConnected(Socket socket) {
                socketList.add(socket);
                runOnUI(() -> onClientConnected(socket));
            }

            @Override
            public void onDisconnected(Socket socket) {
                socketList.remove(socket);
                runOnUI(() -> onClientDisconnected(socket));
            }

            @Override
            public void onStopped(TCPServer tcpServer) {
                if (ipPublisher != null) {
                    ipPublisher.interrupt();
                    ipPublisher = null;
                }
            }


            @Override
            public void onReceived(Socket socket, String data) {
                runOnUI(() -> onClientDataReceived(socket, data));
            }
        });
    }

    @Override
    public void stopTCPServer() {
        if (isTCPServerRunning())
            tcpServer.stop();
    }

    @Override
    public boolean isTCPServerRunning() {
        return tcpServer != null && tcpServer.isRunning();
    }

    @Override
    public void startMediaServer(Video video) {
        stopMediaServer();
        try {
            mediaStreamingServer = new FileServer(Constant.SERVER_STREAM_VIDEO_PORT, new File(video.path));
            mediaStreamingServer.start();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void stopMediaServer() {
        if (mediaStreamingServer != null) {
            mediaStreamingServer.closeAllConnections();
            mediaStreamingServer.stop();
        }
    }

    @Override
    public String getHost() {
        return Networks.getLocalIpAddress();
    }

    @Override
    public void onClientConnected(Socket socket) {
        for (IDataReceiver dataReceiver : dataReceiverList)
            dataReceiver.onClientConnected(socket);
    }

    @Override
    public void onClientDisconnected(Socket socket) {
        for (IDataReceiver dataReceiver : dataReceiverList)
            dataReceiver.onClientDisconnected(socket);
    }

    @Override
    public void onClientDataReceived(Socket socket, @NonNull String data) {
        for (IDataReceiver dataReceiver : dataReceiverList)
            dataReceiver.onClientDataReceived(socket, data);
    }

    @Override
    public void sendData(Socket socket, @NonNull String data) {
        tcpServer.sendTo(socket, data);
    }

    @Override
    public void sendData(Socket socket, @NonNull Object data) {
        tcpServer.sendTo(socket, serializeObject(data));
    }

    @Override
    public void sendData(@NonNull String data) {
        for (Socket socket : socketList)
            sendData(socket, data);
    }

    @Override
    public void sendData(@NonNull Object data) {
        for (Socket socket : socketList)
            sendData(socket, serializeObject(data));
    }

    @Override
    public boolean isClientConnected(Socket socket) {
        return Lists.contains(socketList, new Comparator<Socket, Socket>(socket) {
            @Override
            public boolean compare(Socket one, Socket other) {
                return one.getInetAddress().getHostAddress().equals(other.getInetAddress().getHostAddress()) && other.getLocalPort() == one.getLocalPort();
            }
        });
    }

    @Override
    public void disconnectClient(Socket socket) {
        try {
            Lists.getNullSafe(socketList, new Comparator<Socket, Socket>(socket) {
                @Override
                public boolean compare(Socket one, Socket other) {
                    return one.getInetAddress().getHostAddress().equals(other.getInetAddress().getHostAddress()) && other.getLocalPort() == one.getLocalPort();
                }
            }).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String serializeObject(@NonNull Object object) {
        return object.getClass().getSimpleName() + "@" + new Gson().toJson(object);
    }

    @Override
    @Nullable
    public Object deserializeObject(@NonNull String object) {
        if (object.contains("@")) {
            String[] params = object.split("@");
            if (params.length == 2) {
                String className = params[0];
                String data = params[1];
                try {
                    DexFile dexFile = new DexFile(getPackageCodePath());
                    for (Enumeration<String> classNames = dexFile.entries(); classNames.hasMoreElements(); )
                        if (classNames.nextElement().contains(className))
                            return new Gson().fromJson(data, getClassLoader().loadClass(className));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public int connectedClientsCount() {
        return socketList.size();
    }

    @Override
    public boolean hasConnectedClients() {
        return !socketList.isEmpty();
    }

    @Override
    public boolean hasActiveSyncSessions() {
        return false;
    }

    @Override
    public boolean isClientSyncing(Socket socket) {
        return false;
    }

    @Override
    public boolean isClientSyncFinished(Socket socket, @NonNull String hash) {
        return false;
    }

    @Override
    public boolean isClientSyncFinished(Socket socket, List<String> hashList) {
        return false;
    }

    @Override
    public void startSyncSession(Socket socket, @NonNull String hash) {
        sendData(socket, "stopSyncSession@" + hash);
    }

    @Override
    public void startSyncSession(Socket socket, List<String> hashList) {
        for (String hash : hashList)
            sendData(socket, "stopSyncSession@" + hash);
    }

    @Override
    public void stopSyncSession(Socket socket) {
        sendData(socket, "stopSyncSession@all");
    }

    @Override
    public void stopSyncSession() {
        sendData("stopSyncSession@all");
    }

    @Override
    public void requestSync(Socket socket, @NonNull Video video) {
        sendData(socket, String.format("requestSync@%s@%s", "http://" + getHost() + ":" + Constant.SERVER_STREAM_VIDEO_PORT, new Gson().toJson(video)));
    }

    @Override
    public void requestSync(@NonNull Video video) {
        sendData(String.format("requestSync@%s@%s", "http://" + getHost() + ":" + Constant.SERVER_STREAM_VIDEO_PORT, new Gson().toJson(video)));
    }

    @Override
    public void requestSync(Socket socket, List<Video> videoList) {
        for (Video video : videoList)
            requestSync(socket, video);
    }

    @Override
    public void requestSync(List<Video> videoList) {
        for (Video video : videoList)
            requestSync(video);
    }

    @Override
    public void requestSyncStatus(Socket socket, @NonNull String hash) {
        sendData(socket, "requestSyncStatus@" + hash);
    }

    private void collectVideos() {
        allVideosRetrieved = false;
        if (videoList == null)
            videoList = new ArrayList<>();
        videoList.clear();
        List<File> files = Files.getAllVideos(this, new File(Environment.getExternalStorageDirectory(), "VRVideos"));
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            Video video = new Video(file);
            video.thumb = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            video.path = file.getPath().trim();
            video.name = file.getName().trim();
            video.size = file.length();
            video.hash = Hash.get(file, HashAlgorithm.MD5);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(file.getAbsolutePath());
            video.duration = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            video.resolution = String.format("%sx%s", metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH), metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            metaRetriever.release();
            videoList.add(video);
        }
        allVideosRetrieved = true;
    }

    @Override
    public List<Video> getVideoList() {
        return videoList;
    }

    @Override
    public boolean allRetrievedLoaded() {
        return allVideosRetrieved;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTCPServer();
    }

    public static IServer getServer() {
        return server;
    }
}
