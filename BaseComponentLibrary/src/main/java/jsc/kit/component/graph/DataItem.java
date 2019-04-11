package jsc.kit.component.graph;


public class DataItem {
    private float ratio;//比率, 柱形高度百分比

    public DataItem() {
    }

    public DataItem(float ratio) {
        this.ratio = ratio;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
