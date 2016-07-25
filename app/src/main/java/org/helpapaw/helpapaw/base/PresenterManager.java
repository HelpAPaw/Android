package org.helpapaw.helpapaw.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by iliyan on 6/22/16
 */
public class PresenterManager {
    private static PresenterManager instance;

    private Map<String, Presenter> presenterMap;

    private PresenterManager() {
        this.presenterMap = new HashMap<>();
    }

    public synchronized static PresenterManager getInstance() {
        if (instance == null) {
            instance = new PresenterManager();
        }
        return instance;
    }

    public void putPresenter(String id, Presenter presenter) {
        presenterMap.put(id, presenter);
    }

    public <T extends Presenter> T getPresenter(String id) {
        return (T) presenterMap.get(id);
    }

    public void remove(String id) {
        presenterMap.remove(id);
    }

    public void removeAll() {
        presenterMap.clear();
    }
}
