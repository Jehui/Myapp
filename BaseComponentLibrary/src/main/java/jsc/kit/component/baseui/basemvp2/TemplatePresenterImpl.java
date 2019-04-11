package jsc.kit.component.baseui.basemvp2;


public class TemplatePresenterImpl implements TemplateContract.Presenter {


    @Override
    public void attachModel(TemplateContract.Model model) {

    }

    @Override
    public boolean isModelAttached() {
        return false;
    }

    @Override
    public TemplateContract.Model model() {
        return null;
    }

    @Override
    public void attachView(TemplateContract.View view) {

    }

    @Override
    public boolean isViewAttached() {
        return false;
    }

    @Override
    public TemplateContract.View view() {
        return null;
    }
}
