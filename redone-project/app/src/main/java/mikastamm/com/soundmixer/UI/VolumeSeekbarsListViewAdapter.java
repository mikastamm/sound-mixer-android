package mikastamm.com.soundmixer.UI;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import mikastamm.com.soundmixer.AudioSessionDelegate;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 09.04.2018.
 */

public class VolumeSeekbarsListViewAdapter extends ArrayAdapter<AudioSession> {
    private List<AudioSession> listElements;
    private Activity activity;
    private AudioSessionDelegate vmDelegate;

    public VolumeSeekbarsListViewAdapter(Activity activity, List<AudioSession> audioSessions, AudioSessionDelegate vmDelegate) {
        super(activity, 0, audioSessions);
        listElements = audioSessions;
        this.activity = activity;
        this.vmDelegate = vmDelegate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AudioSession vm = listElements.get(position);

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

        setupButton(vm, imgBtn);

        setupSeekbar(vsbSeekBar, vm);

        return convertView;
    }

    private void setupButton(final AudioSession vm, ToggleSquareImageButton imgBtn){
        if(vm.icon != null)
            imgBtn.setFalseDrawable(new BitmapDrawable(activity.getResources(), vm.icon));
        else
            imgBtn.setFalseDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_application));

        imgBtn.setTrueDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_no_audio));

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioSession old = vm.copy();
                vm.mute = !vm.mute;
                vmDelegate.audioSessionEdited(old, vm);
            }
        });

        imgBtn.setValue(vm.mute);
    }

    private void setupSeekbar(SeekBar vsbSeekBar, final AudioSession vm){
        //Set Volume level
        vsbSeekBar.setProgress((int) (vm.volume * 100));
        vsbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isTracking = false;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(fromUser) {
                    AudioSession old = vm.copy();
                    vm.volume = i / 100f;
                    vmDelegate.audioSessionEdited(old, vm);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                AudioSession old = vm.copy();
                vm.isTracking = true;
                vmDelegate.audioSessionEdited(old, vm);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioSession old = vm.copy();
                vm.isTracking = false;
                vmDelegate.audioSessionEdited(old, vm);
            }
        });
    }
}
