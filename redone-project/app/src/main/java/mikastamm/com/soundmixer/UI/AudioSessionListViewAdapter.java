package mikastamm.com.soundmixer.UI;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 09.04.2018.
 */

public class AudioSessionListViewAdapter extends ArrayAdapter<AudioSession> {
    private ArrayList<AudioSession> listElements;
    private HashMap<Integer, Drawable> progressBarDrawables;
    private boolean refreshProgressDrawables = false;
    private Activity activity;

    public AudioSessionListViewAdapter(Activity activity, ArrayList<AudioSession> audioSessions) {
        super(activity, 0, audioSessions);
        listElements = audioSessions;
        this.activity = activity;
        progressBarDrawables = new HashMap<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AudioSession vm = listElements.get(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.volume_slider_list_element, parent, false);
        }

        TextView txtApplicationName = convertView.findViewById(R.id.txtApplicationName);
        final SeekBar vsbSeekBar = convertView.findViewById(R.id.vsbSeekBar);
        View divider = convertView.findViewById(R.id.volumeListViewDivider);
        final ToggleSquareImageButton imgBtn = convertView.findViewById(R.id.iconBtn);
        FrameLayout frlShadows = convertView.findViewById(R.id.frlShadowContainer);

        // Populate the data into the template view using the data object
        //Set Title
        String title = vm.title.substring(0,1).toUpperCase() + vm.title.substring(1);
        txtApplicationName.setText(title);

        //Set Volume level
        vsbSeekBar.setProgress((int) (vm.volume * 100));

        //Set image
        if(vm.icon != null)
            imgBtn.setFalseDrawable(new BitmapDrawable(activity.getResources(), vm.icon));
        else
            imgBtn.setFalseDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_application));

        imgBtn.setTrueDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_no_audio));

        imgBtn.setValue(vm.mute);

        return convertView;
    }
}
