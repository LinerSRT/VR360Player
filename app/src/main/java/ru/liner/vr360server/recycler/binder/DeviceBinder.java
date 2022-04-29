package ru.liner.vr360server.recycler.binder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360server.recycler.genericadapter.GenericBinder;
import ru.liner.vr360server.recycler.model.Client;
import ru.liner.vr360server.recycler.model.ClientStatus;
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
    private ImageView deviceDisconnect;

    @Override
    public void declareViews() {
        deviceHostName = find(R.id.deviceHostName);
        deviceStatusText = find(R.id.deviceStatusText);
        deviceStatusProgressBar = find(R.id.deviceStatusProgressBar);
        deviceProgressBarData = find(R.id.deviceProgressBarData);
        deviceDisconnect = find(R.id.deviceDisconnect);
    }

    @Override
    public void bindData(RecyclerView recyclerView, GenericAdapter.ViewHolder<Client> viewHolder, Client data) {
        deviceStatusProgressBar.setVisibility(View.GONE);
        deviceProgressBarData.setVisibility(View.GONE);
        deviceStatusText.setVisibility(View.GONE);
        deviceHostName.setText(String.format("%s (%s)", data.getName(), data.getHost()));
        switch (data.getStatus()) {
            case ClientStatus.WAITING:
                deviceStatusText.setVisibility(View.VISIBLE);
                deviceStatusText.setText("Waiting for action");
                break;
            case ClientStatus.UNKNOWN:
                deviceStatusText.setVisibility(View.VISIBLE);
                deviceStatusText.setText("Unknown status");
                break;
            case ClientStatus.DOWNLOADING_VIDEO:
                deviceStatusText.setVisibility(View.VISIBLE);
                deviceProgressBarData.setVisibility(View.VISIBLE);
                deviceStatusProgressBar.setVisibility(View.VISIBLE);
                deviceStatusText.setText("Receiving video");
                deviceStatusProgressBar.setProgress(data.getDownloadProgress());
                break;
            case ClientStatus.READY:
                deviceStatusText.setVisibility(View.VISIBLE);
                deviceStatusText.setText("Ready for play");
                break;
        }
//        devicePlayVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
//                if (device != null) {
//                    device.send("play".getBytes(StandardCharsets.UTF_8));
//                }
//            }
//        });
//        devicePauseVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
//                if (device != null) {
//                    device.send("pause");
//                }
//            }
//        });
//        deviceStopVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TCPDevice device = MainActivity.tcpServer.getClient(data.getHost());
//                if (device != null) {
//                    device.send("stop");
//                }
//            }
//        });
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
