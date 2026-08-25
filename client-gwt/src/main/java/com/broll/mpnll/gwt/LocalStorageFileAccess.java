package com.broll.mpnll.gwt;

import com.broll.mpnll.client.persist.IFileAccess;
import com.google.gwt.storage.client.Storage;

public class LocalStorageFileAccess implements IFileAccess {

    private final Storage storage = Storage.getLocalStorageIfSupported();
    private final String key;
    private String fallback;

    public LocalStorageFileAccess(String key) {
        this.key = key;
    }

    @Override
    public boolean exists() {
        return storage != null ? storage.getItem(key) != null : fallback != null;
    }

    @Override
    public String read() {
        return storage != null ? storage.getItem(key) : fallback;
    }

    @Override
    public void write(String content) {
        if (storage != null) {
            storage.setItem(key, content);
        } else {
            fallback = content;
        }
    }

    @Override
    public void delete() {
        if (storage != null) {
            storage.removeItem(key);
        } else {
            fallback = null;
        }
    }
}
