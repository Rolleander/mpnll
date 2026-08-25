package com.broll.mpnll.client.persist;

/**
 * Non-persistent fallback used until a platform-specific file access is configured.
 */
public class MemoryFileAccess implements IFileAccess {

    private String content;

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
