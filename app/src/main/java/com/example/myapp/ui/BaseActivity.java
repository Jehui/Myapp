package com.example.myapp.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ActionMenuView;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.myapp.R;
import jsc.kit.component.baseui.BaseAppCompatActivity;
import jsc.kit.component.baseui.transition.TransitionProvider;
import jsc.kit.component.utils.CompatResourceUtils;
import jsc.kit.component.utils.WindowUtils;

public abstract class BaseActivity extends BaseAppCompatActivity {
    private TextView tvTitle;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    //ActionBar位于Activity的顶部，可用来显示activity的标题、Icon、Actions和一些用于交互的View。它也可被用于应用的导航。
    protected void initActionBar(ActionBar actionBar) {
        if (actionBar == null)
            return;

        int padding = CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_12);
        FrameLayout customView = new FrameLayout(this);//帧布局
        ActionBar.LayoutParams barParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowUtils.getActionBarSize(this));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView, barParams);
        //添加标题
        tvTitle = new TextView(this);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvTitle.setGravity(Gravity.CENTER);
        customView.addView(tvTitle, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        //添加返回按钮
        //ivBack = new ImageView(this);
        //ivBack.setPadding(padding / 2, 0, padding / 2, 0);
        //ivBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
       // ivBack.setImageResource(R.drawable.ic_chevron_left_white_24dp);
      //ivBack.setBackground(WindowUtils.getSelectableItemBackgroundBorderless(this));
        //customView.addView(ivBack, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
       // ivBack.setOnClickListener(new View.OnClickListener() {
          //  @Override
         //   public void onClick(View v) {
         //       onBackPressed();
         //   }
       // });
//        添加menu菜单
//        actionMenuView = new ActionMenuView(this);
//        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        menuParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
//        customView.addView(actionMenuView, menuParams);
    }

//    public ActionMenuView getActionMenuView() {
//        return actionMenuView;
//    }
//
//    public final void showTitleBarBackView(boolean show) {
//        if (ivBack != null)
//            ivBack.setVisibility(show ? View.VISIBLE : View.GONE);
//    }

    public final void setTitleBarTitle(CharSequence title) {
        if (tvTitle != null)
            tvTitle.setText(title);
    }

    public final void setTitleBarTitle(@StringRes int resId) {
        if (tvTitle != null)
            tvTitle.setText(resId);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

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
        return TransitionProvider.createTransition(getIntent().getStringExtra("transition"), 300L);
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


}
