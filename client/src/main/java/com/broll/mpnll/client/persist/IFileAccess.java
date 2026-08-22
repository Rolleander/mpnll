package com.broll.mpnll.client.persist;

public interface IFileAccess {

    boolean exists();

    String read();

    void write(String content);

    void delete();

}
