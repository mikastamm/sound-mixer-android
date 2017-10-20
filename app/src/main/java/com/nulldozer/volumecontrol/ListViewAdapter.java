package com.nulldozer.volumecontrol;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mika on 27.04.2016.
 */
public class ListViewAdapter extends ArrayAdapter<VolumeData> {

        ArrayList<VolumeData> listElements;
        HashMap<String, Bitmap> sessionIcons;
        HashMap<Integer, Drawable> progressBarDrawables;
        boolean refreshProgressDrawables = false;
        MainActivity main;

        Pair<Integer, Boolean> muteChanged;

        public ListViewAdapter(Context context, ArrayList<VolumeData> users) {
            super(context, 0, users);
            listElements = users;
            main = (MainActivity)context;
            sessionIcons = new HashMap<>();
            progressBarDrawables = new HashMap<>();
        }

        @Override
        public void clear()
        {
            listElements.clear();
            MainActivity.Instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final VolumeData vm = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.array_adapter_seek, parent, false);
            }

            // Lookup view for data population
            TextView txtApplicationName = (TextView) convertView.findViewById(R.id.txtApplicationName);
            final SeekBar vsbSeekBar = (SeekBar) convertView.findViewById(R.id.vsbSeekBar);
            View divider = convertView.findViewById(R.id.volumeListViewDivider);
            final SquareImageButton imgBtn = (SquareImageButton) convertView.findViewById(R.id.iconBtn);
            FrameLayout frlShadows = (FrameLayout)convertView.findViewById(R.id.frlShadowContainer);
//            LinearLayout ticksLeft = (LinearLayout)convertView.findViewById(R.id.llTicksLeft);



            if(refreshProgressDrawables)
            {
                int progress = vsbSeekBar.getProgress();
                Rect bounds = vsbSeekBar.getProgressDrawable().getBounds();
                progressBarDrawables.put(position, MainActivity.seekbar_progress_drawable.getConstantState().newDrawable());
                vsbSeekBar.setProgressDrawable(progressBarDrawables.get(position));
                vsbSeekBar.getProgressDrawable().setBounds(bounds);
                vsbSeekBar.setMax(0);
                vsbSeekBar.setMax(100);
                vsbSeekBar.setProgress(progress);
            }

            if(MainActivity.nightmode)
            {
                txtApplicationName.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorTextNight));
                frlShadows.setBackgroundResource(R.color.colorBackgroundSecondaryNight);
            }
            else{
                txtApplicationName.setTextColor(ContextCompat.getColor(MainActivity.Instance, R.color.colorText));
                frlShadows.setBackgroundResource(R.color.colorBackground);
            }

            if(MainActivity.hideApplicationIcons)
            {
                imgBtn.setVisibility(View.GONE);
            }

            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vm.mute = !vm.mute;
                    vm.ignoreNextMute = true;
                    MainActivity.Instance.clientFragment.clientThread.sendVolumeData(vm);
                    Log.i("ListViewAdapter", "Mute: " + vm.mute);
                    notifyDataSetChanged();
                }
            });

            vsbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (MainActivity.Instance.clientFragment.clientThread.connected) {
                            if(!MainActivity.reduceSliderSensitivity || !vm.sentLast) {
                                vm.volume = progress / 100f;
                                MainActivity.Instance.clientFragment.clientThread.sendVolumeData(vm);

                                if(MainActivity.reduceSliderSensitivity)
                                {
                                    vm.sentLast = true;
                                }
                            }
                            else{
                                vm.sentLast = false;
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (MainActivity.Instance.clientFragment.clientThread.connected) {
                        MainActivity.Instance.clientFragment.clientThread.startTracking(vm);
                        vm.isTracking = true;
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (MainActivity.Instance.clientFragment.clientThread.connected) {

                        if(MainActivity.reduceSliderSensitivity)
                        {
                            vm.volume = vsbSeekBar.getProgress() / 100f;
                            MainActivity.Instance.clientFragment.clientThread.sendVolumeData(vm);
                        }

                        MainActivity.Instance.clientFragment.clientThread.endTracking(vm);
                        vm.isTracking = false;
                    }
                }
            });

            // Populate the data into the template view using the data object
            String title = vm.title.substring(0,1).toUpperCase() + vm.title.substring(1);
            txtApplicationName.setText(title);

            if(!vm.isTracking)
            vsbSeekBar.setProgress((int) (vm.volume * 100));

            final int anim_duration = 250;
            if(vm.mute)
            {
                if(vm.ignoreNextMute) {
                    final ValueAnimator scaleDown = ValueAnimator.ofFloat(1, 0);
                    Easing easing = new Easing(anim_duration);
                    scaleDown.setEvaluator(easing);
                    scaleDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float scaleVal = (float) scaleDown.getAnimatedValue();
                            imgBtn.setScaleX(scaleVal);
                            imgBtn.setScaleY(scaleVal);
                        }
                    });
                    scaleDown.setDuration(anim_duration);
                    scaleDown.start();
                }

                imgBtn.setBackgroundResource(MainActivity.mute_icon_res_id);

                if(vm.ignoreNextMute) {
                    final ValueAnimator scaleUp = ValueAnimator.ofFloat(0, 1);
                    Easing easing = new Easing(anim_duration);
                    scaleUp.setEvaluator(easing);
                    scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float scaleVal = (float) scaleUp.getAnimatedValue();
                            imgBtn.setScaleX(scaleVal);
                            imgBtn.setScaleY(scaleVal);
                        }
                    });
                    scaleUp.setDuration(anim_duration);
                    scaleUp.start();
                }
            }
            else {
                if(vm.ignoreNextMute) {
                    final ValueAnimator scaleDown = ValueAnimator.ofFloat(1, 0);
                    Easing easing = new Easing(anim_duration);
                    scaleDown.setEvaluator(easing);
                    scaleDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float scaleVal = (float) scaleDown.getAnimatedValue();
                            imgBtn.setScaleX(scaleVal);
                            imgBtn.setScaleY(scaleVal);
                        }
                    });
                    scaleDown.setDuration(anim_duration);
                    scaleDown.start();
                }

                if (sessionIcons.get(vm.id) != null) {
                    if (android.os.Build.VERSION.SDK_INT < 16) {
                        imgBtn.setBackgroundDrawable(new BitmapDrawable(main.getResources(), sessionIcons.get(vm.id)));
                    } else {
                        imgBtn.setBackground(new BitmapDrawable(main.getResources(), sessionIcons.get(vm.id)));
                    }
                } else {
                    imgBtn.setBackgroundResource(MainActivity.application_icon_res_id);
                }

                if(vm.ignoreNextMute) {
                    final ValueAnimator scaleUp = ValueAnimator.ofFloat(0, 1);
                    Easing easing = new Easing(anim_duration);
                    scaleUp.setEvaluator(easing);
                    scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float scaleVal = (float) scaleUp.getAnimatedValue();
                            imgBtn.setScaleX(scaleVal);
                            imgBtn.setScaleY(scaleVal);
                        }
                    });
                    scaleUp.setDuration(anim_duration);
                    scaleUp.start();
                }
            }

            if(vm.title.equals("Master")) //TODO: Use multilanguage title
            {
                txtApplicationName.setTypeface(null, Typeface.BOLD);
              //  divider.setLayoutParams(new LinearLayout.LayoutParams(4, ViewGroup.LayoutParams.MATCH_PARENT));
                if(!vm.mute)
                imgBtn.setBackgroundResource(MainActivity.master_icon_res_id);
            }
            else if(vm.title.equals("System"))//TODO: Use multilanguage title
            {
                if(!vm.mute)
                imgBtn.setBackgroundResource(MainActivity.system_icon_res_id);
            }

            if(position == listElements.size()-1)
            {
                refreshProgressDrawables = false;
            }


//            int height = vsbSeekBar.getWidth();
//            int distance = height / 100;
//            Resources res = MainActivity.Instance.getResources();
//
//            for(int i = 0; i <= 100; i++)
//            {
//                View tick = new View(MainActivity.Instance);
//
//                LinearLayout.LayoutParams params;
//                if(i % 10 == 0)
//                {
//                    params = new LinearLayout.LayoutParams((int)res.getDimension(R.dimen.tick_long_width), (int)res.getDimension(R.dimen.tick_height));
//                    params.setMargins(0, 0, 0, distance);
//                    tick.setBackgroundResource(R.drawable.tick_long);
//                }
//                else{
//                    params = new LinearLayout.LayoutParams((int)res.getDimension(R.dimen.tick_short_width), (int)res.getDimension(R.dimen.tick_height));
//                    params.setMargins(0, 0, 0, distance);
//                    tick.setBackgroundResource(R.drawable.tick_short);
//                }
//                tick.setLayoutParams(params);
//                tick.setVisibility(View.VISIBLE);
//                ticksLeft.addView(tick);
//            }

            // Return the completed view to render on screen

            return convertView;

        }

    class MutableBoolean{
        public boolean value;
    }

    }

