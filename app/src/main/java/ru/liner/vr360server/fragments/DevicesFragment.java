package ru.liner.vr360server.fragments;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.net.Socket;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.ClientAdapter;
import ru.liner.vr360server.utils.ViewUtils;
import ru.liner.vr360server.views.ExtraPaddingLinearLayoutManager;
import ru.liner.vr360server.views.SwipeButton;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class DevicesFragment extends BaseFragment {
    private ClientAdapter clientAdapter;
    private RecyclerView socketRecycler;
    private SwipeButton startServerButton;
    private SwipeButton playButton;
    private TextView socketRecyclerEmpty;
    private TextView connectedDeviceCount;

    public DevicesFragment(IServer server) {
        super(server);
    }

    public DevicesFragment() {
        this.server = MainActivity.getServer();
    }

    @Override
    public void declareViews(View view) {
        socketRecycler = find(R.id.socketRecycler);
        startServerButton = find(R.id.startServerButton);
        playButton = find(R.id.playButton);
        socketRecyclerEmpty = find(R.id.socketRecyclerEmpty);
        connectedDeviceCount = find(R.id.connectedDeviceCount);
    }

    @Override
    public void onFragmentCreated() {
        clientAdapter = new ClientAdapter(server);
        socketRecycler.setLayoutManager(new ExtraPaddingLinearLayoutManager(getContext(), 0, ViewUtils.dpToPx(150)));
        socketRecycler.setAdapter(clientAdapter);
        startServerButton.setStateCallback((swipeButton, enabled, fromUser) -> {
            if (enabled) {
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                server.startTCPServer();
            } else {
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_primary));
                server.stopTCPServer();
            }
        });
        playButton.setEnabled(server.hasConnectedClients() && !server.hasActiveSyncSessions() && server.hasSelectVideo());
        playButton.setStateCallback((swipeButton, enabled, fromUser) -> {
            if(fromUser){
                if (enabled) {
                    playButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                    server.sendData("playVideo");
                } else {
                    playButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_green));
                    server.sendData("stopVideo");
                }
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.devices_fragment;
    }

    @Override
    public void onClientDataReceived(Socket socket, @NonNull String data) {
        super.onClientDataReceived(socket, data);
        if(data.equals("syncFinished"))
            playButton.setEnabled(server.hasConnectedClients() && !server.hasActiveSyncSessions() && server.hasSelectVideo());
    }

    @Override
    public void onClientConnected(Socket socket) {
        super.onClientConnected(socket);
        clientAdapter.add(socket);
        ViewUtils.setVisibility(socketRecyclerEmpty, clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        ViewUtils.setVisibility(connectedDeviceCount, clientAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
        connectedDeviceCount.setText(String.format("Connected %s device(s)", server.connectedClientsCount()));
    }

    @Override
    public void onClientDisconnected(Socket socket) {
        super.onClientDisconnected(socket);
        clientAdapter.remove(socket);
        ViewUtils.setVisibility(socketRecyclerEmpty, clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        ViewUtils.setVisibility(connectedDeviceCount, clientAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
        connectedDeviceCount.setText(String.format("Connected %s device(s)", server.connectedClientsCount()));
    }
}
