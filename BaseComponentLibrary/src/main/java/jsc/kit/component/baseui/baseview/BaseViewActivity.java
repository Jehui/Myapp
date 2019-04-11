package jsc.kit.component.baseui.baseview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.Transition;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import jsc.kit.component.R;
import jsc.kit.component.baseui.BaseAppCompatActivity;
import jsc.kit.component.utils.CompatResourceUtils;

public abstract class BaseViewActivity extends BaseAppCompatActivity implements BaseViewCreateDelegate {

    public BaseViewProvider baseViewProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        baseViewProvider = new BaseViewProvider(this);
        baseViewProvider.setBaseViewCreateDelegate(this);
        setContentView(baseViewProvider.provide(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setEmptyViewEnable(boolean enable){
        if (baseViewProvider.getEmptyView() != null)
            baseViewProvider.getEmptyView().setEnabled(enable);
    }

    public void setErrorViewEnable(boolean enable){
        if (baseViewProvider.getErrorView() != null)
            baseViewProvider.getErrorView().setEnabled(enable);
    }

    public abstract void reload();



    @Override
    public void onDownloadProgress(int downloadedBytes, int totalBytes, int downStatus) {

    }

    @Override
    public void onDownloadCompleted(Uri uri) {

    }

    @Override
    public void handleUIMessage(Message msg) {

    }

    @Override
    public void handleWorkMessage(Message msg) {

    }

    @Override
    public Transition createEnterTransition() {
        return null;
    }

    @Override
    public Transition createExitTransition() {
        return null;
    }

    @Override
    public Transition createReturnTransition() {
        return null;
    }

    @Override
    public Transition createReenterTransition() {
        return null;
    }

    @Override
    public void initSharedElement() {

    }



    @Nullable
    @Override
    public View createTitleBar(@NonNull Context context) {
        return null;
    }

    @Nullable
    @Override
    public View createContentView(@NonNull Context context) {
        return null;
    }

    @Nullable
    @Override
    public View createEmptyView(@NonNull Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Empty!!!\nClick to reload.");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!baseViewProvider.isLoading()) {
                    baseViewProvider.showLoadingPage(null);
                    reload();
                }
            }
        });
        return textView;
    }

    @Nullable
    @Override
    public View createLoadingView(@NonNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        //
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        layout.addView(progressBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Loading...!!!\nPlease wait a minute.");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_8);
        layout.addView(textView, params);
        return layout;
    }

    @Nullable
    @Override
    public View createErrorView(@NonNull Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Error!!!\nClick to reload.");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!baseViewProvider.isLoading()) {
                    baseViewProvider.showLoadingPage(null);
                    reload();
                }
            }
        });
        return textView;
    }

    @Override
    @Deprecated
    public void addCustomView(@NonNull Context context, @NonNull ConstraintLayout constraintLayout, @NonNull ConstraintSet constraintSet) {

    }

    @Override
    public void initCustomView(@NonNull Context context, @NonNull RelativeLayout rootView) {

    }
}
