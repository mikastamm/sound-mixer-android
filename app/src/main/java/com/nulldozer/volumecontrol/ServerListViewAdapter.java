package com.nulldozer.volumecontrol;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mika on 23.08.2017.
 */
public class ServerListViewAdapter extends ArrayAdapter<VolumeServer> {

    ArrayList<VolumeServer> listElements;
    VolumeServer activeServer;

    final String TAG = "ServerListViewAdapter";

    public ServerListViewAdapter(Context context, ArrayList<VolumeServer> users) {
        super(context, 0, users);
        listElements = users;
    }

    public VolumeServer getItem(int index) {
        return this.listElements.get(index);
    }

    public VolumeServer getItem(String RsaPublicKey) {
        for(VolumeServer s : listElements)
        {
            if(s.RSAPublicKey.equals(RsaPublicKey))
            {
                return s;
            }
        }
        return null;
    }

    public void removeActive(){
        if(MainActivity.Instance.clientFragment.clientThread != null)
        MainActivity.Instance.clientFragment.clientThread.listenerThread.interrupt();
        
        if(activeServer != null)
        activeServer.active = false;

        activeServer = null;
        notifyDataSetChanged();
    }

    public VolumeServer getPasswordlessServer(){
        for(VolumeServer s : listElements)
        {
            if(!s.hasPassword)
                return s;
        }
        return null;
    }

    public void setActive(int position){
        VolumeServer newActive = getItem(position);
        setActive(newActive);
    }

    public void setActive(VolumeServer newActive){

        if (activeServer != null) {
            activeServer.active = false;
        }

        if(newActive != null) {
            newActive.active = true;

            if(MainActivity.Instance.clientFragment.clientThread != null)
                MainActivity.Instance.clientFragment.clientThread.close();

            activeServer = newActive;

            MainActivity.Instance.clientFragment.clientThread = new ClientThread();
        }
        else{
            if(listElements.size() > 0)
            {
                Log.i(TAG, "setActive(int position): Volume Server at $position not found, setting first element in ListView as active");
                setActive(0);
            }
            else{
                Log.i(TAG, "setActive(int position): Volume Server at $position not found and ListView empty, no active set");
            }
        }

        notifyDataSetChanged();
        MainActivity.Instance.listViewAdapterVolumeSliders.refreshProgressDrawables = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position

        VolumeServer vm = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.servers_list_element, parent, false);

        }

        // Lookup view for data population

        ImageView serverIcon = (ImageView) convertView.findViewById(R.id.serverIcon);
        TextView serverName = (TextView) convertView.findViewById(R.id.tvServerName);
        TextView serverIP = (TextView) convertView.findViewById(R.id.tvServerIP);

        // Populate the data into the template view using the data object
        if(Settings.nightmode)
        {
            serverName.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
            serverIP.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
        }
        else{
            serverName.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
            serverIP.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
        }


        if(!Settings.showServerIpInServerBrowser)
        {
            serverIP.setVisibility(View.GONE);
        }
        else{
            serverIP.setVisibility(View.VISIBLE);
        }

        serverIP.setText(vm.IPAddress);
        serverName.setText(vm.name);


        if(vm.active)
        {
            serverIcon.setImageResource(R.mipmap.server_active_icon);
            serverName.setTypeface(null, Typeface.BOLD);
            activeServer = vm;
        }
        else{
            serverIcon.setImageResource(R.mipmap.server_icon);
            serverName.setTypeface(null, Typeface.NORMAL);
            serverIcon.setImageResource(IconResourceIDs.system_icon_res_id);
        }


        // Return the completed view to render on screen

        return convertView;

    }

}
