package ru.liner.vr360server.recycler.binder;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.charset.StandardCharsets;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360server.recycler.genericadapter.GenericBinder;
import ru.liner.vr360server.recycler.model.Client;
import ru.liner.vr360server.tcp.TCPDevice;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class DeviceBinder extends GenericBinder<Client> {
    private TextView deviceHostName;
    private TextView deviceStatusText;
    private ProgressBar deviceStatusProgressBar;
    private TextView deviceProgressBarData;
    private Button devicePlayVideo;
    private Button devicePauseVideo;
    private Button deviceStopVideo;
    private Button deviceDisconnect;

    @Override
    public void declareViews() {
        deviceHostName = find(R.id.deviceHostName);
        deviceStatusText = find(R.id.deviceStatusText);
        deviceStatusProgressBar = find(R.id.deviceStatusProgressBar);
        deviceProgressBarData = find(R.id.deviceProgressBarData);
        devicePlayVideo = find(R.id.devicePlayVideo);
        devicePauseVideo = find(R.id.devicePauseVideo);
        deviceStopVideo = find(R.id.deviceStopVideo);
        deviceDisconnect = find(R.id.deviceDisconnect);
    }

    @Override
    public void bindData(RecyclerView recyclerView, GenericAdapter.ViewHolder<Client> viewHolder, Client data) {
        deviceHostName.setText(String.format("%s (%s)", data.getName(), data.getHost()));
        devicePlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
                if (device != null) {
                    device.send("play".getBytes(StandardCharsets.UTF_8));
                }
            }
        });
        devicePauseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
                if (device != null) {
                    device.send("pause");
                }
            }
        });
        deviceStopVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
                if (device != null) {
                    device.send("stop");
                }
            }
        });
        deviceDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
                if (device != null) {
                    device.send("disconnect");
                }
            }
        });
    }
}
