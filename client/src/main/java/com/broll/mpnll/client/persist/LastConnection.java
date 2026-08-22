package com.broll.mpnll.client.persist;

public class LastConnection {

    private IFileAccess fileAccess;

    public LastConnection(IFileAccess fileAccess) {
        this.fileAccess = fileAccess;
    }

    public String getLastConnection() {
        if (this.fileAccess.exists()) {
            return this.fileAccess.read();
        }
        return null;
    }

    public void setLastConnection(String connection) {
        fileAccess.write(connection);
    }

    public void clear() {
        if (this.fileAccess.exists()) {
            this.fileAccess.delete();
        }
    }


}
