package jsc.kit.component.baseui;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInVisible();
        }
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected void onInVisible() {
    }

    protected abstract void lazyLoad();
}
