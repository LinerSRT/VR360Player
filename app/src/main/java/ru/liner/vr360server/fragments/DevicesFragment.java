package ru.liner.vr360server.fragments;

import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.recycler.adapter.ClientAdapter;
import ru.liner.vr360server.server.ClientStatus;
import ru.liner.vr360server.server.ConnectedClient;
import ru.liner.vr360server.server.DownloadingStatus;
import ru.liner.vr360server.server.PlayingStatus;
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
    private SwipeButton startPlayButton;
    private TextView socketRecyclerEmpty;

    public DevicesFragment(IServer server) {
        super(server);
    }


    @Override
    public void declareViews(View view) {
        socketRecycler = find(R.id.socketRecycler);
        startServerButton = find(R.id.startServerButton);
        startPlayButton = find(R.id.startPlayButton);
        socketRecyclerEmpty = find(R.id.socketRecyclerEmpty);
    }

    @Override
    public void onFragmentCreated() {
        clientAdapter = new ClientAdapter(server);
        socketRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        socketRecycler.setAdapter(clientAdapter);
        startPlayButton.setStateCallback((swipeButton, enabled, fromUser) -> {
            if (enabled) {
                startPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
            } else {
                startPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_green));
            }
        });
        startPlayButton.setEnabled(false);
        startServerButton.setStateCallback((swipeButton, enabled, fromUser) -> {
            if (enabled) {
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                server.showNotification("Server started!", "Waiting for client connection", R.color.primaryColor);
                startPlayButton.setEnabled(true);
                server.startServer();

            } else {
                startPlayButton.disableButton(false);
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_primary));
                server.showNotification("Server stopped!", "All connections has been closed", R.color.red);
                startPlayButton.setEnabled(false);
                server.stopServer();

            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.devices_fragment;
    }

    @Override
    public void onSocketConnected(Socket socket, int position) {
        super.onSocketConnected(socket, position);
        clientAdapter.add(socket);
        socketRecyclerEmpty.setVisibility(clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSocketDisconnected(Socket socket, int position) {
        super.onSocketDisconnected(socket, position);
        clientAdapter.remove(socket);
        socketRecyclerEmpty.setVisibility(clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void onClientStatusChanged(Socket socket, ClientStatus clientStatus){
        ConnectedClient client = clientAdapter.get(socket);
        if(client != null){
            client.clientStatus = clientStatus;
            clientAdapter.update(client);
        }
    }

    private void onDownloadingStatusChanged(Socket socket, DownloadingStatus downloadingStatus){
        ConnectedClient client = clientAdapter.get(socket);
        if(client != null){
            client.downloadingStatus = downloadingStatus;
            clientAdapter.update(client);
        }
    }

    private void onPlayingStatusChanged(Socket socket, PlayingStatus playingStatus){
        ConnectedClient client = clientAdapter.get(socket);
        if(client != null){
            client.playingStatus = playingStatus;
            clientAdapter.update(client);
        }
    }



    @Override
    public void onReceived(Socket socket, String command) {
        super.onReceived(socket, command);
        if(command.contains("@")){
            String[] data = command.split("@");
            if(data.length == 2){
                switch (data[0]){
                    case "ClientStatus":
                        onClientStatusChanged(socket, new Gson().fromJson(data[1], ClientStatus.class));
                        break;
                    case "DownloadingStatus":
                        onDownloadingStatusChanged(socket, new Gson().fromJson(data[1], DownloadingStatus.class));
                        break;
                    case "PlayingStatus":
                        onPlayingStatusChanged(socket, new Gson().fromJson(data[1], PlayingStatus.class));
                        break;
                }
            }
        }
    }

}
