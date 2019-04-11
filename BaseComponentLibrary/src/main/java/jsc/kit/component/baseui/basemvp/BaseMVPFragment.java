package jsc.kit.component.baseui.basemvp;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import jsc.kit.component.baseui.BaseLazyLoadFragment;

public abstract class BaseMVPFragment extends BaseLazyLoadFragment {

    private List<BasePresenter> presenterManager = null;

    /**
     * Add presenter into presenter manager.
     *
     * @param presenter presenter instance
     */
    public final void addToPresenterManager(@NonNull BasePresenter presenter) {
        if (presenterManager == null) {
            presenterManager = new ArrayList<>();
        }
        presenterManager.add(presenter);
    }

    /**
     * Remove presenter from presenter manager.
     *
     * @param presenter presenter instance
     */
    public final void removeFromPresenterManager(@NonNull BasePresenter presenter) {
        if (presenterManager != null && !presenterManager.isEmpty()) {
            presenterManager.remove(presenter);
        }
    }

    /**
     * Release presenters' resources.
     */
    public void recyclePresenterResources() {
        if (presenterManager != null && !presenterManager.isEmpty()) {
            for (BasePresenter presenter : presenterManager) {
                presenter.release();
                presenter = null;
            }
        }
    }

    @Override
    public void onDestroyView() {
        recyclePresenterResources();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
