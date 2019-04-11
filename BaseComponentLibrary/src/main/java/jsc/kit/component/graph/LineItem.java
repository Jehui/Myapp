package jsc.kit.component.graph;

import android.graphics.Color;
import android.support.annotation.ColorInt;


public class LineItem {
    private int lineColor = Color.BLUE;
    private DataItem[] data = null;

    public LineItem() {
    }

    public LineItem(@ColorInt int lineColor, DataItem[] data) {
        this.lineColor = lineColor;
        this.data = data;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
    }

    public DataItem[] getData() {
        return data;
    }

    public void setData(DataItem[] data) {
        this.data = data;
    }
}
