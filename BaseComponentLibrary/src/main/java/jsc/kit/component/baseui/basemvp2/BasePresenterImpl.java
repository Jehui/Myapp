package jsc.kit.component.baseui.basemvp2;

import java.lang.ref.WeakReference;


public abstract class BasePresenterImpl<M extends IBaseModel, V extends IBaseView> {
    private WeakReference<M> modelReference = null;
    private WeakReference<V> viewReference = null;

    public BasePresenterImpl() {
        attachModel(null);
        attachView(null);
    }

    public BasePresenterImpl(V v) {
        attachModel(null);
        attachView(v);
    }

    public BasePresenterImpl(M m, V v) {
        attachModel(m);
        attachView(v);
    }

    public void attachModel(M m) {
        modelReference = m == null ? null : new WeakReference<>(m);
    }

    public boolean isModelAttached() {
        return modelReference != null && modelReference.get() != null;
    }

    public M model() {
        return modelReference == null ? null : modelReference.get();
    }

    public void attachView(V v) {
        viewReference = v == null ? null : new WeakReference<>(v);
    }

    public boolean isViewAttached() {
        return viewReference != null && viewReference.get() != null;
    }

    public V view() {
        return viewReference == null ? null : viewReference.get();
    }
}
