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

class SidebarController {

    boolean sideBarExpanded = false;

    private static final int SIDEBAR_OPEN_DURATION = 250;
    private static final int SIDEBAR_CLOSE_DURATION = 250;
    private static final int SIDEBAR_LISTENER_CLEAR_PADDING = 20;

    private MainActivity activity;
    private boolean isLandscape;

    SidebarController(final MainActivity activity, boolean isLandscapeOrientation){
        this.activity = activity;
        this.isLandscape = isLandscapeOrientation;

        final ImageButton btnExpand = (ImageButton) activity.findViewById(R.id.btnExpand);
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
                PopupMenu popup = new PopupMenu(activity, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.overflow_menu_main, popup.getMenu());

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String title = item.getTitle().toString();

                        if(title.equals(activity.getString(R.string.main_menu_refresh)))
                        {
                            new NetworkDiscoveryThread(activity).start();
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_how_to_use)))
                        {
                            //New Instruction Dialog Fragment
                            //showInstructionsDialog();
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_settings)))
                        {
                            Intent settingsIntent = new Intent(activity, SettingsActivity.class);
                            activity.startActivity(settingsIntent);
                        }
                        else if(title.equals(activity.getString(R.string.main_menu_feedback)))
                        {
                            FeedbackDialog feedbackDialog = new FeedbackDialog();
                            feedbackDialog.setMainActivity(activity);
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

    void toggleSidebar(){
        final ImageView expandImg = (ImageView) activity.findViewById(R.id.expandImg);
        final LinearLayout sideBarContentLL = (LinearLayout) activity.findViewById(R.id.sideBarContentLL);

        int targetRotation;
        int targetDimension;
        int animDuration;

        if(isLandscape && sideBarExpanded)
        {
            targetRotation = 0;
            targetDimension = 0;
            animDuration = SIDEBAR_CLOSE_DURATION;
        }
        else if(isLandscape && !sideBarExpanded)
        {
            targetRotation = 180;
            targetDimension = activity.getWindowManager().getDefaultDisplay().getWidth() / 4;
            animDuration = SIDEBAR_OPEN_DURATION;
        }
        else if(!isLandscape && sideBarExpanded)
        {
            targetRotation = 90;
            targetDimension = 0;
            animDuration = SIDEBAR_CLOSE_DURATION;
        }
        else
        {
            targetRotation = 270;
            targetDimension = activity.getWindowManager().getDefaultDisplay().getHeight() / 6;
            animDuration = SIDEBAR_OPEN_DURATION;
        }

        final ValueAnimator rotate = ValueAnimator.ofFloat(expandImg.getRotation(), targetRotation);
        Easing easing = new Easing(animDuration);
        rotate.setEvaluator(easing);
        rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float rotVal = (float) valueAnimator.getAnimatedValue();
                expandImg.setRotation(rotVal);
            }
        });
        rotate.setDuration(animDuration);
        rotate.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SIDEBAR_CLOSE_DURATION + SIDEBAR_LISTENER_CLEAR_PADDING);
                }
                catch (InterruptedException ignored){}
                finally {
                    rotate.removeAllUpdateListeners();
                }
            }
        }).start();

        final ValueAnimator anim = ValueAnimator.ofInt( (isLandscape ? sideBarContentLL.getMeasuredWidth() : sideBarContentLL.getMeasuredHeight()), targetDimension);
        anim.setEvaluator(easing);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = sideBarContentLL.getLayoutParams();

                if(isLandscape)
                    layoutParams.width = val;
                else
                    layoutParams.height = val;

                sideBarContentLL.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(animDuration);
        anim.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SIDEBAR_CLOSE_DURATION + SIDEBAR_LISTENER_CLEAR_PADDING);
                }
                catch (InterruptedException ignored){}
                finally {
                    anim.removeAllUpdateListeners();
                }
            }
        }).start();

        //  sideBarContentLL.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        sideBarExpanded = !sideBarExpanded;
    }

}
