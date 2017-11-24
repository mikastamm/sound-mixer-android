package com.nulldozer.volumecontrol;

import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Mika on 02.11.2017.
 */

public class ServerRefreshByUser {
    public SwipeRefreshLayout swipeContainer;
    public View refreshServersTip;
    private final MainActivity mainActivity;

    public ServerRefreshByUser(MainActivity activity){
        mainActivity = activity;
        if(Build.VERSION.SDK_INT >= 19 && !Settings.useAlternativeServerRefresh)
        {
            refreshServersTip = activity.findViewById(R.id.llPullToRefresh);

            ((ImageView)activity.findViewById(R.id.imgPullDown)).setImageResource(R.mipmap.swipe_down_light_grey_icon);

            swipeContainer = (SwipeRefreshLayout) activity.findViewById(R.id.swipeContainer);

            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new NetworkDiscoveryThread(mainActivity).start();
                }
            });
            
        }
        else
        {
            refreshServersTip = activity.findViewById(R.id.btnResearchServers);

            if(Settings.useAlternativeServerRefresh && Build.VERSION.SDK_INT >= 19)
            {
                swipeContainer.setEnabled(false);
                swipeContainer.setRefreshing(false);
            }

            refreshServersTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NetworkDiscoveryThread(mainActivity).start();
                    refreshServersTip.setEnabled(false);
                }
            });
        }
    }
}
