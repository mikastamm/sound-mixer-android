package mikastamm.com.soundmixer.Helpers;

import android.animation.TypeEvaluator;

public class Easing implements TypeEvaluator<Number> {

    float duration;

    public Easing(float duration)
    {
        this.duration = duration;
    }

    @Override
    public Number evaluate(float fraction, Number startValue, Number endValue) {
        float t = duration * fraction;
        float b = startValue.floatValue();
        float c = endValue.floatValue() - startValue.floatValue();
        float d = duration;
        float result = calculate(t,b,c,d);
        return result;

    }

    public float calculate(float t, float b, float c, float d){
        t /= d/2;
        if (t < 1) {
            return c/2*t*t*t + b;
        }
        t -= 2;
        return c/2*(t*t*t + 2) + b;
    }


}
