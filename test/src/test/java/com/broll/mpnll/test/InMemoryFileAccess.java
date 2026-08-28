package com.broll.mpnll.test;

import com.broll.mpnll.client.persist.IFileAccess;

final class InMemoryFileAccess implements IFileAccess {

    private String content;

    InMemoryFileAccess() {
    }

    InMemoryFileAccess(String content) {
        this.content = content;
    }

    @Override
    public boolean exists() {
        return content != null;
    }

    @Override
    public String read() {
        return content;
    }

    @Override
    public void write(String content) {
        this.content = content;
    }

    @Override
    public void delete() {
        content = null;
    }
}
