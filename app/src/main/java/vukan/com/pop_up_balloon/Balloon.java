package vukan.com.pop_up_balloon;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import vukan.com.pop_up_balloon.utils.PixelHelper;

@SuppressLint("AppCompatCustomView")
public class Balloon extends ImageView implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    private ValueAnimator mAnimator;
    private BalloonListener mListener;
    private boolean mPopped;

    public Balloon(Context context) {
        super(context);
    }

    public Balloon(@NonNull Context context, int color, int rawHeight) {
        super(context);
        mListener = (BalloonListener) context;
        this.setImageResource(R.drawable.balloon);
        this.setColorFilter(color);
        setLayoutParams(new ViewGroup.LayoutParams(PixelHelper.pixelsToDp(rawHeight / 2, context), PixelHelper.pixelsToDp(rawHeight, context)));
    }

    public void releaseBalloon(int screenHeight, int duration) {
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(screenHeight, 0f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setTarget(this);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
        setY((float) valueAnimator.getAnimatedValue());
    }

    @Override
    public void onAnimationStart(Animator animator) {
    }

    @Override
    public void onAnimationEnd(Animator animator) {
        if (!mPopped) mListener.popBalloon(this, false);
    }

    @Override
    public void onAnimationCancel(Animator animator) {
    }

    @Override
    public void onAnimationRepeat(Animator animator) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!mPopped && event.getAction() == MotionEvent.ACTION_DOWN) {
            mListener.popBalloon(this, true);
            mPopped = true;
            mAnimator.cancel();
        }

        return super.onTouchEvent(event);
    }

    public void setPopped(boolean isBalloonPopped) {
        mPopped = isBalloonPopped;
        if (isBalloonPopped) mAnimator.cancel();
    }

    public interface BalloonListener {
        void popBalloon(Balloon balloon, boolean userTouch);
    }
}