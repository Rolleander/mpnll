package com.broll.mpnll.client.persist;

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
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i == 8 || i == 12 || i == 16 || i == 20) {
                key.append('-');
            }
            int value = (int) (Math.random() * 16);
            key.append(Integer.toHexString(value));
        }
        return key.toString();
    }

}
