package com.nulldozer.volumecontrol;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

/**
 * Created by Mika on 02.11.2017.
 */

public class SidebarController {

    public boolean sideBarExpanded = false;

    private AppCompatActivity activity;
    private boolean isLandscape;

    public SidebarController(final AppCompatActivity activity, boolean isLandscapeOrientation){
        this.activity = activity;
        this.isLandscape = isLandscapeOrientation;

        final ImageButton btnExpand = (ImageButton) activity.findViewById(R.id.btnExpand);
        final LinearLayout sideBarContentLL = (LinearLayout) activity.findViewById(R.id.sideBarContentLL);
        final ImageButton btnPupupMenu = (ImageButton) activity.findViewById(R.id.imgBtnPopupMenuMain);
        btnPupupMenu.setFocusable(false);

        final GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(sideBarExpanded)
                {
                    if(e1.getX() < e2.getX())
                    {
                        toggleSidebar();
                    }
                }
                else{
                    toggleSidebar();
                }
                return true;
            }
        };

        final GestureDetector detector = new GestureDetector(activity, gestureListener);

        btnExpand.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                detector.onTouchEvent(e);
                return false;
            }
        });

        btnPupupMenu.setImageResource(R.mipmap.popup_menu_icon);
        btnPupupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.Instance, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_main, popup.getMenu());

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String title = item.getTitle().toString();

                        if(title.equals(activity.getString(R.string.main_menu_refresh)))
                        {
                            new NetworkDiscoveryThread().start();
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_how_to_use)))
                        {
                            //New Instruction Dialog Fragment
                            //showInstructionsDialog();
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_settings)))
                        {
                            Intent settingsIntent = new Intent(MainActivity.Instance, SettingsActivity.class);
                            activity.startActivity(settingsIntent);
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_feedback)))
                        {
                            FeedbackDialog feedbackDialog = new FeedbackDialog();
                            feedbackDialog.setFeedbackType(FeedbackDialog.FeedbackType.NEUTRAL_FEEDBACK);
                            feedbackDialog.show(activity.getSupportFragmentManager(), "menu-feedback-dialog");
                        }

                        return true;
                    }
                });
            }
        });

        btnExpand.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                toggleSidebar();
            }
        });
    }

    public void toggleSidebar(){
        final ImageButton btnExpand = (ImageButton) activity.findViewById(R.id.btnExpand);
        final ImageView expandImg = (ImageView) activity.findViewById(R.id.expandImg);
        final LinearLayout sideBarContentLL = (LinearLayout) activity.findViewById(R.id.sideBarContentLL);

        if(isLandscape) {
            if (sideBarExpanded) {
                //   btnExpand.setImageResource(R.mipmap.expand_right_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 0);
                Easing easing = new Easing(350);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(350);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredWidth(), 0);

                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.width = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(350);
                anim.start();

                //  sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = false;
            } else {
                //   btnExpand.setImageResource(R.mipmap.collapse_left_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 180);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredWidth(), activity.getWindowManager().getDefaultDisplay().getWidth() / 5);
                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.width = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //    sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = true;
            }
        }
        else
        {
            if (sideBarExpanded) {
                //   btnExpand.setImageResource(R.mipmap.expand_right_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 90);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredHeight(), 0);

                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.height = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //  sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = false;
            } else {
                //   btnExpand.setImageResource(R.mipmap.collapse_left_icon);

                ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), 270);
                Easing easing = new Easing(250);
                rotate.setEvaluator(easing);
                rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float rotVal = (float) valueAnimator.getAnimatedValue();
                        expandImg.setRotation(rotVal);
                    }
                });
                rotate.setDuration(250);
                rotate.start();

                ValueAnimator anim = ValueAnimator.ofInt(sideBarContentLL.getMeasuredHeight(), activity.getWindowManager().getDefaultDisplay().getHeight() / 6);
                anim.setEvaluator(easing);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();
                        layoutParams.height = val;
                        sideBarContentLL.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(250);
                anim.start();

                //    sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT));
                sideBarExpanded = true;
            }
        }
    }
}
