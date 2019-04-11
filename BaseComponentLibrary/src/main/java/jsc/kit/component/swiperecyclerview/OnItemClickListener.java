package jsc.kit.component.swiperecyclerview;

import android.view.View;


public interface OnItemClickListener<T> {
    void onItemClick(View itemView, int position, T item);
}
