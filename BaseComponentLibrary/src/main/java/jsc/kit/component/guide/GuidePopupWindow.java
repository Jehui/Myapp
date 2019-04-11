package jsc.kit.component.guide;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import jsc.kit.component.R;
import jsc.kit.component.utils.CompatResourceUtils;
import jsc.kit.component.utils.WindowUtils;


public final class GuidePopupWindow {

    private PopupWindow mPopupWindow;
    private GuideLayout guideLayout;
    private View target;
    private int yOffset, minRippleSize, maxRippleSize;
    private GuideLayout.OnRippleViewUpdateLocationCallback onRippleViewUpdateLocationCallback;

    public GuidePopupWindow(Context context) {
        this(
                context,
                WindowUtils.getStatusBarHeight(context),
                CompatResourceUtils.getDimensionPixelSize(context, R.dimen.space_32),
                CompatResourceUtils.getDimensionPixelSize(context, R.dimen.space_64)
        );
    }

    /**
     * @param context       context
     * @param yOffset       offset from top
     * @param minRippleSize minimum ripple size
     * @param maxRippleSize max ripple size
     */
    public GuidePopupWindow(Context context, int yOffset, int minRippleSize, int maxRippleSize) {
        this.minRippleSize = Math.min(minRippleSize, maxRippleSize);
        this.maxRippleSize = Math.max(minRippleSize, maxRippleSize);
        this.yOffset = yOffset;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        guideLayout = new GuideLayout(context);
        guideLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mPopupWindow = new PopupWindow();
        mPopupWindow.setContentView(guideLayout);
        mPopupWindow.setWidth(metrics.widthPixels);
        mPopupWindow.setHeight(metrics.heightPixels);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
    }

    @NonNull
    public GuideLayout getGuideLayout() {
        return guideLayout;
    }

    public GuidePopupWindow removeAllCustomView() {
        guideLayout.removeAllCustomViews();
        return this;
    }

    public GuidePopupWindow setBackgroundColor(@ColorInt int color) {
        guideLayout.setBackgroundColor(color);
        return this;
    }

    /**
     * It's invalid after {@link #attachTarget(View)}.
     *
     * @param yOffset status bar height
     * @return {@link GuidePopupWindow}
     */
    public GuidePopupWindow setyOffset(int yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    /**
     * It's invalid after {@link #attachTarget(View)}.
     *
     * @param minRippleSize minimum ripple view size
     * @return {@link GuidePopupWindow}
     */
    public GuidePopupWindow setMinRippleSize(int minRippleSize) {
        this.minRippleSize = minRippleSize;
        return this;
    }

    /**
     * It's invalid after {@link #attachTarget(View)}.
     *
     * @param maxRippleSize maximum ripple size
     * @return {@link GuidePopupWindow}
     */
    public GuidePopupWindow setMaxRippleSize(int maxRippleSize) {
        this.maxRippleSize = maxRippleSize;
        return this;
    }

    /**
     * It's invalid after {@link #attachTarget(View)}.
     *
     * @param onRippleViewUpdateLocationCallback maximum ripple size
     * @return {@link GuidePopupWindow}
     */
    public GuidePopupWindow setOnRippleViewUpdateLocationCallback(GuideLayout.OnRippleViewUpdateLocationCallback onRippleViewUpdateLocationCallback) {
        this.onRippleViewUpdateLocationCallback = onRippleViewUpdateLocationCallback;
        return this;
    }

    public GuidePopupWindow attachTarget(@NonNull View mTarget) {
        this.target = mTarget;
        this.target.setDrawingCacheEnabled(true);
        guideLayout.updateTargetLocation(mTarget, yOffset, minRippleSize, maxRippleSize, onRippleViewUpdateLocationCallback);
        this.target.postDelayed(new Runnable() {
            @Override
            public void run() {
                target.setDrawingCacheEnabled(false);
            }
        }, 300);
        return this;
    }

    /**
     * It must be called after {@link #attachTarget(View)} if necessary.
     * <br>And you should call it before {@link #show()}.
     *
     * @param customView custom view
     * @param callback   initialize call back.
     * @param <V>        custom view type
     */
    public <V extends View> GuidePopupWindow addCustomView(@NonNull V customView, @NonNull GuideLayout.OnAddCustomViewCallback<V> callback) {
        if (target == null)
            throw new IllegalStateException("You need attach target first.");
        guideLayout.addCustomView(customView, callback);
        return this;
    }

    public GuidePopupWindow addTargetClickListener(@Nullable final OnCustomClickListener listener) {
        guideLayout.setTargetClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null)
                    listener.onCustomClick(v);
            }
        });
        return this;
    }

    public GuidePopupWindow addCustomClickListener(@NonNull View customView, @Nullable final OnCustomClickListener listener, final boolean needDismiss) {
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needDismiss)
                    dismiss();

                if (listener != null)
                    listener.onCustomClick(v);
            }
        });
        return this;
    }

    /**
     * Before showing action, it must had attached target.
     */
    public void show() {
        if (target == null)
            throw new IllegalStateException("You need attach target first.");
        mPopupWindow.showAsDropDown(target, -guideLayout.getTargetRect().left, -guideLayout.getTargetRect().bottom);
    }

    public void dismiss() {
        if (mPopupWindow == null)
            return;
        mPopupWindow.dismiss();
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }
}
