package jsc.kit.component.swiperecyclerview;

import android.view.View;


public interface OnItemLongClickListener<T> {
    boolean onItemLongClick(View itemView, int position, T item);
}
