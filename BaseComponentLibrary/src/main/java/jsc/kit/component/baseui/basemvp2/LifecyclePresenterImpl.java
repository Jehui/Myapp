package jsc.kit.component.baseui.basemvp2;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;


public class LifecyclePresenterImpl extends BasePresenterImpl<LifecycleContract.Model, LifecycleContract.View> implements LifecycleContract.Presenter {

    @Override
    public void start() {
        if (isViewAttached())
            view().onLifecycleStart();
    }

    @Override
    public void resume() {
        if (isViewAttached())
            view().onLifecycleResume();
    }

    @Override
    public void pause() {
        if (isViewAttached())
            view().onLifecyclePause();
    }

    @Override
    public void stop() {
        if (isViewAttached())
            view().onLifecycleStop();
    }

    @Override
    public void destroy() {
        if (isViewAttached())
            view().onLifecycleDestroy();
    }
}
