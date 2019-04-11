package jsc.kit.component.swiperecyclerview;

import java.util.List;


public interface ViewAdapter<T> {

    public List<T> getItems();

    public T getItemAt(int position);

    public void setItems(List<T> items);

    public void addItems(List<T> items);

    public void addItems(int position, List<T> items);

    public void addItem(T item);

    public void addItem(int position, T item);

    public void removeItem(int position);

    public void removeItem(T item);
}
