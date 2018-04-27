package mikastamm.com.soundmixer.UI;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mikastamm.com.soundmixer.AudioSessionDelegate;
import mikastamm.com.soundmixer.AudioSessionIconList;
import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 09.04.2018.
 */

public class VolumeSeekbarsListViewAdapter extends ArrayAdapter<AudioSession> {
    private List<AudioSession> listElements;
    private Activity activity;
    private AudioSessionDelegate vmDelegate;

    private final String masterTitle = "Master";
    private final String systemTitle = "System";

    public VolumeSeekbarsListViewAdapter(Activity activity, List<AudioSession> audioSessions, AudioSessionDelegate vmDelegate) {
        super(activity, 0, audioSessions);
        listElements = audioSessions;
        this.activity = activity;
        this.vmDelegate = vmDelegate;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AudioSession vm = listElements.get(position);

        correctListOrderIfNecessary(vm, position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.volume_slider_list_element, parent, false);
        }

        TextView txtApplicationName = convertView.findViewById(R.id.txtApplicationName);
        final SeekBar vsbSeekBar = convertView.findViewById(R.id.vsbSeekBar);
        final ToggleSquareImageButton imgBtn = convertView.findViewById(R.id.iconBtn);

        // Populate the data into the template view using the data object
        //Set Title
        String title = vm.title.substring(0, 1).toUpperCase() + vm.title.substring(1);
        txtApplicationName.setText(title);

        setupButton(vm, imgBtn);

        setupSeekbar(vsbSeekBar, vm);

        if (vm.title.equals(masterTitle))
            overwriteForMasterAudioSession(imgBtn);

        return convertView;
    }

    private void correctListOrderIfNecessary(AudioSession vm, int position){
        if(vm.title.equals(masterTitle) && position != 0)
        {
            Collections.swap(listElements, 0, position);
            notifyDataSetChanged();
        }
        else if(vm.title.equals(systemTitle) && position != 1)
        {
            Collections.swap(listElements, 1, position);
            notifyDataSetChanged();
        }
    }

    private void overwriteForMasterAudioSession(ToggleSquareImageButton imgBtn) {
        imgBtn.setFalseDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_audio));
    }

    private void setupButton(final AudioSession vm, ToggleSquareImageButton imgBtn) {
        AudioSessionIconList iconList = AudioSessionIconList.getInstance();
        if (iconList.get(vm.id) != null)
            imgBtn.setFalseDrawable(new BitmapDrawable(activity.getResources(), iconList.get(vm.id).icon));
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

    private void setupSeekbar(SeekBar vsbSeekBar, final AudioSession vm) {
        //Set Volume level
        vsbSeekBar.setProgress((int) (vm.volume * 100));
        vsbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean sendNextChange = true;
            boolean isTracking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    if(sendNextChange || !isTracking) {
                        AudioSession old = vm.copy();
                        vm.volume = i / 100f;
                        vmDelegate.audioSessionEdited(old, vm);
                    }
                    else{
                        sendNextChange = true;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //isTracking prevents the Seekbars from getting updated while the user is tracking them
                AudioSession old = vm.copy();
                vm.isTracking = true;
                isTracking = true;
                vmDelegate.audioSessionEdited(old, vm);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioSession old = vm.copy();
                vm.isTracking = false;
                isTracking = false;
                vmDelegate.audioSessionEdited(old, vm);
            }
        });
    }
}
