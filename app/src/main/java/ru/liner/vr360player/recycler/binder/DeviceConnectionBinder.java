package ru.liner.vr360player.recycler.binder;


import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import ru.liner.vr360player.R;
import ru.liner.vr360player.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360player.recycler.genericadapter.GenericBinder;
import ru.liner.vr360player.server.packet.DeviceConnectPacket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class DeviceConnectionBinder extends GenericBinder<DeviceConnectPacket> {
    private TextView host;
    private TextView status;
    @Override
    public void declareViews() {
        host = find(R.id.device_binder_host);
        status = find(R.id.device_binder_status);
    }

    @Override
    public void bindData(RecyclerView recyclerView, GenericAdapter.ViewHolder<DeviceConnectPacket> viewHolder, DeviceConnectPacket data) {
            host.setText(data.host);
    }
}
