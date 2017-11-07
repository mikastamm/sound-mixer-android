package com.nulldozer.volumecontrol;

import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.lucasr.twowayview.TwoWayView;

import static com.nulldozer.volumecontrol.IconResourceIDs.*;

/**
 * Created by Mika on 02.11.2017.
 */

public class Nightmode {
    public static void setEnabled(MainActivity mainActivity, boolean enabled)
    {
        LinearLayout llSidebar = (LinearLayout)mainActivity.findViewById(R.id.sideBarContentLL);
        LinearLayout llExpandSidebar = (LinearLayout)mainActivity.findViewById(R.id.llExpandSidebar);
        ImageView imgPullDown = (ImageView)mainActivity.findViewById(R.id.imgPullDown);
        ImageView imgBtnPopupMenu = (ImageView)mainActivity.findViewById(R.id.imgBtnPopupMenuMain);
        ImageView expandImg = (ImageView) mainActivity.findViewById(R.id.expandImg);
        TextView tvPullDown = (TextView)mainActivity.findViewById(R.id.tvPullToRefresh);
        TwoWayView twoWayViewSliders =(TwoWayView)mainActivity.findViewById(R.id.lvVolumeSliders);
        TwoWayView twoWayViewServers =(TwoWayView)mainActivity.findViewById(R.id.listViewServers);

        if(enabled) {
            master_icon_res_id = R.mipmap.audio_nightmode_icon;
            mute_icon_res_id = R.mipmap.audio_mute_nightmode_icon;
            popup_menu_icon_res_id = R.mipmap.popup_menu_nightmode_icon;
            system_icon_res_id = R.mipmap.server_nightmode_icon;
            pull_down_icon_res_id = R.mipmap.swipe_down_nightmode_icon;
            application_icon_res_id = R.mipmap.application_nightmode_icon;
            expand_right_icon_res_id = R.mipmap.expand_right_nightmode_icon;
            seekbar_progress_drawable = mainActivity.getResources().getDrawable(R.drawable.seekbar_progressbar_nightmode);

            twoWayViewSliders.setBackgroundResource(R.color.colorBackgroundNightLight);
            llSidebar.setBackgroundResource(R.color.colorExpandSidebarButtonNight);
            twoWayViewServers.setBackgroundResource(R.color.colorExpandSidebarButtonNight);
            tvPullDown.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
            llExpandSidebar.setBackgroundResource(R.color.colorExpandSidebarButtonNight);
        }
        else{
            master_icon_res_id = R.mipmap.audio_icon;
            mute_icon_res_id = R.mipmap.audio_mute_icon;
            popup_menu_icon_res_id = R.mipmap.popup_menu_icon;
            system_icon_res_id = R.mipmap.server_icon;
            pull_down_icon_res_id = R.mipmap.swipe_down_grey_icon;
            application_icon_res_id = R.mipmap.application_icon;
            expand_right_icon_res_id = R.mipmap.expand_right_icon;
            seekbar_progress_drawable = mainActivity.getResources().getDrawable(R.drawable.seekbar_progressbar);

            tvPullDown.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
            llSidebar.setBackgroundResource(R.color.colorExpandSidebarButton);
            llExpandSidebar.setBackgroundResource(R.color.colorExpandSidebarButton);
            twoWayViewServers.setBackgroundResource(R.color.colorExpandSidebarButton);
            twoWayViewSliders.setBackgroundResource(R.color.colorBackgroundSecondary);
        }

        imgPullDown.setImageResource(pull_down_icon_res_id);
        imgBtnPopupMenu.setImageResource(popup_menu_icon_res_id);
        expandImg.setImageResource(expand_right_icon_res_id);

        mainActivity.listViewAdapterVolumeSliders.refreshProgressDrawables = true;
        mainActivity.listViewAdapterVolumeSliders.notifyDataSetChanged();
        mainActivity.serverListViewAdapter.notifyDataSetChanged();
    }
}
