package com.wlf.app.main.data;

public class ZipFile extends ContentFile {
    public ZipFile(String pathname) {
        super(pathname);
    }

    public void unpack(String destination) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
