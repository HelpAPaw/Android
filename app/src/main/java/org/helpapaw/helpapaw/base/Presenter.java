package org.helpapaw.helpapaw.base;

/**
 * Created by iliyan on 6/22/16
 */
public abstract class Presenter<T> {

    public Presenter(T view) {
        this.view = view;
    }

    private T view;

    public T getView() {
        return view;
    }

    public void setView(T view) {
        this.view = view;
    }

    public void clearView() {
        this.view = null;
    }

}
