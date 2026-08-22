package com.broll.mpnll.client.persist;

import java.util.UUID;

public class ClientAuthentication {

    private IFileAccess fileAccess;
    private String key;

    public ClientAuthentication(IFileAccess fileAccess) {
        this.fileAccess = fileAccess;
    }

    public String getKey() {
        if (this.key == null) {
            if (this.fileAccess.exists()) {
                this.key = this.fileAccess.read();
            } else {
                this.key = generateAccountKey();
                this.fileAccess.write(this.key);
            }
        }
        return this.key;
    }

    public void clear() {
        if (this.fileAccess.exists()) {
            this.fileAccess.delete();
        }
    }

    private String generateAccountKey() {
        return UUID.randomUUID().toString();
    }

}
