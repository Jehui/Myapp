package jsc.kit.component.widget.spacelickable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import jsc.kit.component.IViewAttrDelegate;


public class SpaceClickableFrameLayout extends FrameLayout implements IViewAttrDelegate {

    private SpaceClickHelper<SpaceClickableFrameLayout> spaceClickHelper = null;

    public SpaceClickableFrameLayout(Context context) {
        super(context);
        initAttr(context, null, 0);
    }

    public SpaceClickableFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs, 0);
    }

    public SpaceClickableFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs, defStyleAttr);
    }

    @Override
    public void initAttr(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        spaceClickHelper = new SpaceClickHelper<>(this);
    }

    public void setOnSpaceClickListener(OnSpaceClickListener<SpaceClickableFrameLayout> onSpaceClickListener) {
        spaceClickHelper.setOnSpaceClickListener(onSpaceClickListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return spaceClickHelper.onTouchEvent(event);
    }
}
