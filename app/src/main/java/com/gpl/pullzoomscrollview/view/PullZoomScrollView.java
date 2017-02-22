package com.gpl.pullzoomscrollview.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ScrollView;

/**
 * designed by gpl
 */
public class PullZoomScrollView extends ScrollView implements View.OnTouchListener {
    //mFirstPosition's value when mZoomView not being scaled
    private static final float POSITION_ORIGIN = -Integer.MAX_VALUE;
    private float maxDistance;
    // the first position that should be recorded
    private float mFirstPosition = POSITION_ORIGIN;
    // show the view scall state
    private Boolean mScaling = false;
    // the view will be scalled
    private View mZoomView;
    //mZoomView original width
    private int mZoomViewOriginalWidth;
    //mZoomView original height
    private int mZoomViewOriginalHeight;

    public PullZoomScrollView(Context context) {
        super(context);
    }

    public PullZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (getChildAt(0) != null) {
            ViewGroup vg = (ViewGroup) getChildAt(0);
            if (vg.getChildAt(0) != null) {
                mZoomView = vg.getChildAt(0);
                setOnTouchListener(this);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mZoomViewOriginalWidth <= 0 || mZoomViewOriginalHeight <= 0) {
            mZoomViewOriginalWidth = mZoomView.getMeasuredWidth();
            mZoomViewOriginalHeight = mZoomView.getMeasuredHeight();
            maxDistance = mZoomViewOriginalHeight * 3 / 4;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //when rootview scrolldistance==0 record the touch position by mFirstPosition
                if (!mScaling && (getScrollY() == 0)) {
                    mFirstPosition = event.getY();
                } else if (!mScaling) break;
                int distance = (int) ((event.getY() - mFirstPosition));
                // when scroll up relative to mFirstPosition we will do nothing
                if (distance < 0) {
                    mScaling = false;
                    break;
                }
                // when scroll down relative to mFirstPosition an from we will scall the mZoomView
                mScaling = true;
                doZoom(distance);
                return true;
            case MotionEvent.ACTION_UP:
                recoverZoomView();
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * zoom view
     *
     * @param s
     */
    public void doZoom(float s) {
        if (s <= 0) return;
        s = s > maxDistance ? maxDistance : s;
        if (mZoomViewOriginalHeight <= 0 || mZoomViewOriginalWidth <= 0) {
            return;
        }

        if (mZoomView instanceof ViewGroup) {
            int childCount = ((ViewGroup) mZoomView).getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    View child = ((ViewGroup) mZoomView).getChildAt(i);
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    lp.width = (int) (mZoomViewOriginalWidth + s);
                    lp.height = (int) (mZoomViewOriginalHeight * ((mZoomViewOriginalWidth + s) / mZoomViewOriginalWidth));
                    child.setLayoutParams(lp);
                }
            }

        } else {
            ViewGroup.LayoutParams lp = mZoomView.getLayoutParams();
            lp.width = (int) (mZoomViewOriginalWidth + s);
            lp.height = (int) (mZoomViewOriginalHeight * ((mZoomViewOriginalWidth + s) / mZoomViewOriginalWidth));
            mZoomView.setLayoutParams(lp);
        }
    }

    /**
     * set zoom view to original state
     */
    public void recoverZoomView() {
        if (!mScaling) return;
        float distance = 0;
        if (mZoomView instanceof ViewGroup) {
            int childCount = ((ViewGroup) mZoomView).getChildCount();
            if (childCount > 0) {
                View child = ((ViewGroup) mZoomView).getChildAt(0);
                distance = child.getMeasuredWidth() - mZoomViewOriginalWidth;
            }
        } else {
            distance = mZoomView.getMeasuredWidth() - mZoomViewOriginalWidth;
        }

        ValueAnimator anim = ObjectAnimator.ofFloat(0.0F, 1.0F).setDuration((long) (distance * 0.618));
        final float finalDistance = distance;
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                doZoom(finalDistance - ((finalDistance) * cVal));
            }
        });
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        mScaling = false;
    }

}

