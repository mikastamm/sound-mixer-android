package mikastamm.com.soundmixer.UI;

import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageButton;

/**
 * Created by Mika on 09.04.2018.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import mikastamm.com.soundmixer.Helpers.Easing;


public class ToggleSquareImageButton extends AppCompatImageButton implements View.OnClickListener {
    public int animDuration = 250;

    private Drawable trueDrawable, falseDrawable;
    private boolean value;

    public ToggleSquareImageButton(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public ToggleSquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public ToggleSquareImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    public void setTrueDrawable(Drawable drawable)
    {
        trueDrawable = drawable;
    }

    public void setFalseDrawable(Drawable drawable)
    {
        falseDrawable = drawable;
    }

    public boolean getValue()
    {
        return value;
    }

    @Override
    public void onClick(View v) {
        setValue(!value);
    }

    public void setValue(boolean nval)
    {
        if(nval == value)
        {
            setBackground((nval ? trueDrawable : falseDrawable));
            return;
        }

        value = nval;

        if(nval)
        {
            scaleToZero();
            setBackground(trueDrawable);
            scaleToVisible();
        }
        else
        {
            scaleToZero();
            setBackground(falseDrawable);
            scaleToVisible();
        }
    }

    private void scaleToZero(){
        final ValueAnimator scaleDown = ValueAnimator.ofFloat(1, 0);
        Easing easing = new Easing(animDuration);
        scaleDown.setEvaluator(easing);
        scaleDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scaleVal = (float) scaleDown.getAnimatedValue();
                setScaleX(scaleVal);
                setScaleY(scaleVal);
            }
        });

        scaleDown.setDuration(animDuration);
        scaleDown.start();
    }

    private void scaleToVisible(){
        final ValueAnimator scaleUp = ValueAnimator.ofFloat(0, 1);
        Easing easing = new Easing(animDuration);
        scaleUp.setEvaluator(easing);
        scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scaleVal = (float) scaleUp.getAnimatedValue();
                setScaleX(scaleVal);
                setScaleY(scaleVal);
            }
        });
        scaleUp.setDuration(animDuration);
        scaleUp.start();
    }

    private int squareDim = 1000000000;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int h = this.getMeasuredHeight();
        int w = this.getMeasuredWidth();
        int curSquareDim = Math.min(w, h);
        // Inside a viewholder or other grid element,
        // with dynamically added content that is not in the XML,
        // height may be 0.
        // In that case, use the other dimension.
        if (curSquareDim == 0)
            curSquareDim = Math.max(w, h);

        if(curSquareDim < squareDim)
        {
            squareDim = curSquareDim;
        }

        setMeasuredDimension(squareDim, squareDim);

    }

}