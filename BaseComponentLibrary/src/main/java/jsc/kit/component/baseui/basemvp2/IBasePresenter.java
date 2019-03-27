package jsc.kit.component.baseui.basemvp2;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * <br><a href="https://github.com/JustinRoom/JSCKit" target="_blank">https://github.com/JustinRoom/JSCKit</a>
 *
 * @author jiangshicheng
 */
public interface IBasePresenter<M, V> {
    public void attachModel(M m);
    public boolean isModelAttached();
    public M model();
    public void attachView(V v);
    public boolean isViewAttached();
    public V view();
}
