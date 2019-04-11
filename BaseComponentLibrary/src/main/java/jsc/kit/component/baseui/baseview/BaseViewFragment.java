package jsc.kit.component.baseui.baseview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import jsc.kit.component.baseui.BaseLazyLoadFragment;
import jsc.kit.component.baseui.baseview.BaseViewProvider;


public abstract class BaseViewFragment extends BaseLazyLoadFragment implements BaseViewCreateDelegate {

    public BaseViewProvider baseViewProvider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        baseViewProvider = new BaseViewProvider(context);
        baseViewProvider.setBaseViewCreateDelegate(this);
    }

    @Override
    protected View createRootView(@NonNull LayoutInflater inflater, ViewGroup container) {
        return initBaseView();
    }

    @Override
    public void initView(View rootView) {
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private View initBaseView() {
        if (baseViewProvider == null)
            return null;

        if (baseViewProvider.getEmptyView() != null) {
            baseViewProvider.getEmptyView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!baseViewProvider.isLoading()) {
                        baseViewProvider.showLoadingPage(null);
                        reLazyLoad();
                    }
                }
            });
        }

        if (baseViewProvider.getErrorView() != null) {
            baseViewProvider.getErrorView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!baseViewProvider.isLoading()) {
                        baseViewProvider.showLoadingPage(null);
                        reLazyLoad();
                    }
                }
            });
        }
        return baseViewProvider.provide();
    }
}
