package ru.liner.vr360player.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import ru.liner.vr360player.CoreActivity;
import ru.liner.vr360player.R;
import ru.liner.vr360player.recycler.binder.DeviceConnectionBinder;
import ru.liner.vr360player.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360player.server.IPConnectionManger;
import ru.liner.vr360player.server.IPPublisher;
import ru.liner.vr360player.server.packet.DeviceConnectPacket;
import ru.liner.vr360player.server.packet.DeviceDisconnectPacket;
import ru.liner.vr360player.server.packet.DevicePacket;
import ru.liner.vr360player.utils.Networks;
import ru.liner.vr360player.views.LImageButton;


@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("WrongConstant")
public class MainActivity extends CoreActivity implements IPConnectionManger.Callback{
    private LImageButton syncDeicesButton;
    private RecyclerView recyclerView;
    private GenericAdapter genericAdapter;
    private IPConnectionManger connectionManger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionManger = new IPConnectionManger(this);
        recyclerView = findViewById(R.id.deviceRecycler);
        syncDeicesButton = findViewById(R.id.syncDevicesButton);
        syncDeicesButton.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        genericAdapter = new GenericAdapter(recyclerView);
        genericAdapter.register(R.layout.device_list_binder, DeviceConnectionBinder.class, DeviceConnectPacket.class);
        recyclerView.setAdapter(genericAdapter);
        connectionManger.start();
        new IPPublisher().start();
    }

    @Override
    public void onConnectionCreated() {
        
    }

    @Override
    public void onConnectionClosed() {
        

    }

    @Override
    public void onException(Exception e) {
        

    }

    @Override
    public void onDeviceConnected(DeviceConnectPacket packet) {
        if(!genericAdapter.getItemsList().contains(packet)) {
            genericAdapter.add(packet);
            recyclerView.scrollToPosition(genericAdapter.getItemCount() - 1);
        }
        
    }

    @Override
    public void onDeviceDisconnected(DeviceDisconnectPacket packet) {
        

    }

    @Override
    public void onDeviceChanged(DevicePacket packet) {
        

    }

    @Override
    public void onRawDataReceived(byte[] bytes) {

        
    }

    @Override
    public String getHostAddress() {
        return Networks.getLocalIpAddress();
    }
}
